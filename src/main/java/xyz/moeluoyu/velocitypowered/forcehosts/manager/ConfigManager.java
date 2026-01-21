package xyz.moeluoyu.velocitypowered.forcehosts.manager;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigManager {
    private final Logger logger;
    private final Path dataDirectory;
    private List<String> allowedHosts = Collections.emptyList();
    private boolean logPing = true; // 默认记录ping日志
    private boolean logConnection = true; // 默认记录连接日志
    
    // 防频繁刷新配置
    private boolean antiSpamEnabled = true;
    private int timeWindow = 10; // 时间窗口（秒）
    private int maxPings = 5; // 最大ping次数
    private int blockDuration = 60; // 阻止时间（秒）
    private boolean logBlock = true; // 默认记录防频繁刷新日志

    public ConfigManager(Logger logger, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configPath = dataDirectory.resolve("config.yml");
            if (!Files.exists(configPath)) {
                try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                    if (in != null) {
                        Files.copy(in, configPath);
                    } else {
                        Files.write(configPath, Collections.singletonList("hosts:\n  - example.com"));
                    }
                }
                logger.info("已创建默认配置文件 config.yml");
            }

            Yaml yaml = new Yaml();
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Map<String, Object> config = yaml.load(reader);

                List<String> hosts = Optional.ofNullable(config)
                        .map(c -> c.get("hosts"))
                        .filter(l -> l instanceof List)
                        .map(l -> (List<?>) l)
                        .map(list -> {
                            List<String> result = new ArrayList<>();
                            for (Object item : list) {
                                if (item != null) {
                                    result.add(item.toString().toLowerCase());
                                }
                            }
                            return ImmutableList.copyOf(result);
                        })
                        .orElse(ImmutableList.of());

                allowedHosts = hosts;
                logger.info("已加载 {} 个允许的主机名", hosts.size());
                
                // 加载logPing配置
                logPing = Optional.ofNullable(config)
                        .map(c -> c.get("logPing"))
                        .filter(o -> o instanceof Boolean)
                        .map(o -> (Boolean) o)
                        .orElse(true); // 默认为true
                // 加载logConnection配置
                logConnection = Optional.ofNullable(config)
                        .map(c -> c.get("logConnection"))
                        .filter(o -> o instanceof Boolean)
                        .map(o -> (Boolean) o)
                        .orElse(true); // 默认为true
                
                // 加载防频繁刷新配置
                Map<String, Object> antiSpamConfig = Optional.ofNullable(config)
                        .map(c -> c.get("antiSpamRefresh"))
                        .filter(m -> m instanceof Map)
                        .map(m -> (Map<String, Object>) m)
                        .orElse(Collections.emptyMap());
                
                antiSpamEnabled = Optional.ofNullable(antiSpamConfig.get("enabled"))
                        .filter(o -> o instanceof Boolean)
                        .map(o -> (Boolean) o)
                        .orElse(true); // 默认启用
                
                timeWindow = Optional.ofNullable(antiSpamConfig.get("timeWindow"))
                        .filter(o -> o instanceof Integer)
                        .map(o -> (Integer) o)
                        .orElse(10); // 默认10秒
                
                maxPings = Optional.ofNullable(antiSpamConfig.get("maxPings"))
                        .filter(o -> o instanceof Integer)
                        .map(o -> (Integer) o)
                        .orElse(5); // 默认5次
                
                blockDuration = Optional.ofNullable(antiSpamConfig.get("blockDuration"))
                        .filter(o -> o instanceof Integer)
                        .map(o -> (Integer) o)
                        .orElse(60); // 默认60秒

                logBlock = Optional.ofNullable(antiSpamConfig.get("logBlock"))
                        .filter(o -> o instanceof Boolean)
                        .map(o -> (Boolean) o)
                        .orElse(true); // 默认记录防频繁刷新日志
            }
        } catch (IOException e) {
            logger.error("加载配置文件失败", e);
        }
    }

    // Getter方法
    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    public boolean isLogPing() {
        return logPing;
    }

    public boolean isLogConnection() {
        return logConnection;
    }

    public boolean isAntiSpamEnabled() { return antiSpamEnabled; }

    public int getTimeWindow() {
        return timeWindow;
    }

    public int getMaxPings() {
        return maxPings;
    }

    public int getBlockDuration() {
        return blockDuration;
    }

    public boolean isLogBlock() {
        return logBlock;
    }
}