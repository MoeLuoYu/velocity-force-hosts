package xyz.moeluoyu.velocitypowered.forcehosts.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MessageManager {
    private final Logger logger;
    private final Path dataDirectory;
    private Map<String, String> messages = Collections.emptyMap();
    private final LegacyComponentSerializer componentSerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .build();

    public MessageManager(Logger logger, Path dataDirectory) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    public void loadMessages() {
        try {
            Path messagesPath = dataDirectory.resolve("messages.yml");
            if (!Files.exists(messagesPath)) {
                try (InputStream in = getClass().getResourceAsStream("/messages.yml")) {
                    if (in != null) {
                        Files.copy(in, messagesPath);
                    } else {
                        Files.write(messagesPath, Arrays.asList(
                                "kick-message: '&c你必须通过正确的主机名连接服务器！'",
                                "plugin-initialized: '&aForceHosts插件已初始化！'",
                                "hosts-loaded: '&a已加载 {} 个允许的主机名'",
                                "config-load-error: '&c加载配置文件失败'",
                                "connection-blocked: '&c阻止了来自 {} 的连接，使用了无效主机名: {}'",
                                "ping-blocked: '&c阻止了来自 {} 的ping请求，使用了无效主机名: {}'",
                                "reload-success: '&a配置和语言文件已成功重载！'",
                                "reload-failed: '&c重载配置时发生错误: {}'",
                                "command-usage: '&e用法: /forcehosts reload - 重新加载配置'",
                                "ping-spam-blocked: '&c阻止了来自 {} 的频繁ping请求，将在 {} 秒后解除阻止'"
                        ));
                    }
                }
                logger.info("已创建默认消息文件 messages.yml");
            }

            Yaml yaml = new Yaml();
            try (Reader reader = Files.newBufferedReader(messagesPath)) {
                Map<String, Object> messageConfig = yaml.load(reader);
                messages = flattenMap(messageConfig);
            }
        } catch (IOException e) {
            logger.error(parseColor("&c加载消息文件失败"), e);
            messages = Collections.singletonMap("kick-message", "&c无法加载消息配置，请联系管理员");
        }
    }

    private Map<String, String> flattenMap(Map<String, Object> source) {
        Map<String, String> result = new HashMap<>();
        flattenMapHelper(source, result, "");
        return result;
    }

    private void flattenMapHelper(Map<String, Object> source, Map<String, String> result, String prefix) {
        source.forEach((key, value) -> {
            String newKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenMapHelper(nestedMap, result, newKey);
            } else {
                result.put(newKey, value != null ? value.toString() : "");
            }
        });
    }

    public String parseColor(String message) {
        if (message == null) return "";
        return message.replace('&', '§');
    }

    public Component toComponent(String message) {
        if (message == null) return Component.empty();
        return componentSerializer.deserialize(message);
    }

    public String getMessageOrDefault(String key, String defaultValue) {
        return messages.getOrDefault(key, defaultValue);
    }
}