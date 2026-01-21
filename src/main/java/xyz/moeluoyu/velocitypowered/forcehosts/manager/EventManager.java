package xyz.moeluoyu.velocitypowered.forcehosts.manager;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;

import java.net.InetSocketAddress;

public class EventManager {
    private final org.slf4j.Logger logger;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final AntiSpamManager antiSpamManager;

    public EventManager(org.slf4j.Logger logger, ConfigManager configManager, MessageManager messageManager, AntiSpamManager antiSpamManager) {
        this.logger = logger;
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.antiSpamManager = antiSpamManager;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        String virtualHost = event.getConnection().getVirtualHost()
                .map(address -> address.getHostString().toLowerCase())
                .orElse("");
        
        String ipAddress = getIPAddress(event.getConnection().getRemoteAddress());

        // 检查是否因频繁ping而被阻止
        if (antiSpamManager.checkAndHandlePingSpam(ipAddress)) {
            // 因频繁ping而被阻止
            event.setResult(ResultedEvent.GenericResult.denied());
            antiSpamManager.logSpamBlock(ipAddress);
            return;
        }

        if (!configManager.getAllowedHosts().contains(virtualHost)) {
            // 如果主机名不在允许列表中，直接阻止ping请求
            event.setResult(ResultedEvent.GenericResult.denied());
            if (configManager.isLogPing()) {
                logger.info(messageManager.parseColor(messageManager.getMessageOrDefault("ping-blocked", "&c阻止了来自 {} 的ping请求，使用了无效主机名: {}")),
                        event.getConnection().getRemoteAddress(), virtualHost);
            }
        }
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String virtualHost = event.getConnection().getVirtualHost()
                .map(address -> address.getHostString().toLowerCase())
                .orElse("");

        if (!configManager.getAllowedHosts().contains(virtualHost)) {
            String kickMessage = messageManager.getMessageOrDefault("kick-message", "&c你必须通过正确的主机名连接服务器！");
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    messageManager.toComponent(kickMessage)
            ));
            // 根据配置决定是否记录连接阻止日志
            if (configManager.isLogConnection()) {
                logger.info(messageManager.parseColor(messageManager.getMessageOrDefault("connection-blocked", "&c阻止了来自 {} 的连接，使用了无效主机名: {}")),
                        event.getConnection().getRemoteAddress(), virtualHost);
            }
        }
    }

    /**
     * 获取IP地址字符串
     * @param address InetSocketAddress
     * @return IP地址字符串
     */
    private String getIPAddress(InetSocketAddress address) {
        if (address == null) {
            return "unknown";
        }
        return address.getAddress().getHostAddress();
    }
}