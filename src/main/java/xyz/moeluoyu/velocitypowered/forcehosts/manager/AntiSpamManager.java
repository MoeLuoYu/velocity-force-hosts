package xyz.moeluoyu.velocitypowered.forcehosts.manager;

import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class AntiSpamManager {
    private final Logger logger;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    
    // 记录每个IP的ping请求
    private final Map<String, List<Long>> pingHistory = new ConcurrentHashMap<>();
    // 记录被阻止的IP及其解除阻止时间
    private final Map<String, Long> blockedIPs = new ConcurrentHashMap<>();
    // 记录每个IP最后一次记录ping-spam-blocked日志的时间
    private final Map<String, Long> lastSpamLogTime = new ConcurrentHashMap<>();
    
    // 用于同步pingHistory操作的锁
    private final Object pingHistoryLock = new Object();

    public AntiSpamManager(Logger logger, MessageManager messageManager, ConfigManager configManager) {
        this.logger = logger;
        this.messageManager = messageManager;
        this.configManager = configManager;
    }

    /**
     * 检查IP是否被阻止
     * @param ipAddress IP地址
     * @return 如果被阻止返回true，否则返回false
     */
    public boolean isIPBlocked(String ipAddress) {
        if (!configManager.isAntiSpamEnabled()) {
            return false;
        }
        
        Long unblockTime = blockedIPs.get(ipAddress);
        if (unblockTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= unblockTime) {
            // 阻止时间已过，从阻止列表中移除
            blockedIPs.remove(ipAddress);
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查并处理频繁ping
     * @param ipAddress IP地址
     * @return 如果应该阻止ping返回true，否则返回false
     */
    public boolean checkAndHandlePingSpam(String ipAddress) {
        if (!configManager.isAntiSpamEnabled()) {
            return false;
        }
        
        // 如果IP已经被阻止，直接返回true
        if (isIPBlocked(ipAddress)) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();

        // 同步块-线程安全
        synchronized (pingHistoryLock) {
            List<Long> pings = pingHistory.computeIfAbsent(ipAddress, k -> new ArrayList<>());
            
            // 创建新的列表来避免并发修改异常
            List<Long> newPings = new ArrayList<>(pings);
            
            // 添加当前ping时间
            newPings.add(currentTime);
            
            // 清理超出时间窗口的ping记录
            long windowStart = currentTime - TimeUnit.SECONDS.toMillis(configManager.getTimeWindow());
            newPings.removeIf(time -> time < windowStart);
            
            // 更新pingHistory
            pingHistory.put(ipAddress, newPings);
            
            // 检查是否超过最大ping次数
            if (newPings.size() > configManager.getMaxPings()) {
                // 超过限制，阻止该IP
                long unblockTime = currentTime + TimeUnit.SECONDS.toMillis(configManager.getBlockDuration());
                blockedIPs.put(ipAddress, unblockTime);
                
                // 清理该IP的ping历史
                pingHistory.remove(ipAddress);
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 记录防频繁刷新日志
     * @param ipAddress IP地址
     */
    public void logSpamBlock(String ipAddress) {
        if (!configManager.isLogBlock()) {
            return; // 如果不记录防频繁刷新日志，则直接返回
        }
        
        // 检查是否需要记录日志（1秒冷却防止重复日志刷屏）
        long currentTime = System.currentTimeMillis();
        Long lastLogTime = lastSpamLogTime.get(ipAddress);
        if (lastLogTime == null || currentTime - lastLogTime >= 1000) { // 1000毫秒 = 1秒
            // 计算剩余阻止时间（秒）
            Long unblockTime = blockedIPs.get(ipAddress);
            int remainingSeconds = 0;
            if (unblockTime != null && unblockTime > currentTime) {
                remainingSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(unblockTime - currentTime);
            }
            
            logger.info(messageManager.parseColor(messageManager.getMessageOrDefault("ping-spam-blocked", "&c阻止了来自 {} 的频繁ping请求，将在 {} 秒后解除阻止")),
                    ipAddress, remainingSeconds);
            
            // 更新最后日志时间
            lastSpamLogTime.put(ipAddress, currentTime);
        }
    }
    
    /**
     * 清理过期的阻止IP
     */
    public void cleanupExpiredBlocks() {
        long currentTime = System.currentTimeMillis();
        blockedIPs.entrySet().removeIf(entry -> currentTime >= entry.getValue());
    }
}