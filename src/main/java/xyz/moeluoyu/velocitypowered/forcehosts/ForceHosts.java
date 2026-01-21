package xyz.moeluoyu.velocitypowered.forcehosts;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import xyz.moeluoyu.velocitypowered.forcehosts.manager.*;

import javax.inject.Inject;
import java.nio.file.Path;

@Plugin(
        id = "forcehosts",
        name = "ForceHosts",
        version = "1.0",
        description = "强制客户端使用指定主机名连接",
        authors = {"MoeLuoYu"}
)
public class ForceHosts {
    private final ProxyServer server;
    private final Logger logger;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final EventManager eventManager;
    private final AntiSpamManager antiSpamManager;

    @Inject
    public ForceHosts(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.configManager = new ConfigManager(logger, dataDirectory);
        this.messageManager = new MessageManager(logger, dataDirectory);
        this.antiSpamManager = new AntiSpamManager(logger, messageManager, configManager);
        this.eventManager = new EventManager(logger, configManager, messageManager, antiSpamManager);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        messageManager.loadMessages(); // 加载消息文件
        configManager.loadConfig(); // 加载配置文件
        server.getEventManager().register(this, eventManager); // 注册事件处理器
        registerCommand(); // 注册命令
        logger.info(messageManager.parseColor(messageManager.getMessageOrDefault("plugin-initialized", "&aForceHosts插件已初始化！")));
    }

    private void registerCommand() {
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("forcehosts")
                        .aliases("fh")
                        .plugin(this)
                        .build(),
                new CommandManager(configManager, messageManager, logger)
        );
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        antiSpamManager.cleanupExpiredBlocks(); // 清理过期的阻止IP
        logger.info(messageManager.parseColor(messageManager.getMessageOrDefault("plugin-shutdown", "&aForceHosts插件已关闭！")));
    }
}