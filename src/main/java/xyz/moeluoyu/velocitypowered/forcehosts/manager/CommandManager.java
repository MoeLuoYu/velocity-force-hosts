package xyz.moeluoyu.velocitypowered.forcehosts.manager;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandManager implements SimpleCommand {
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final org.slf4j.Logger logger;

    public CommandManager(ConfigManager configManager, MessageManager messageManager, org.slf4j.Logger logger) {
        this.configManager = configManager;
        this.messageManager = messageManager;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!source.hasPermission("forcehosts.reload")) {
                source.sendMessage(messageManager.toComponent("&c你没有权限执行此命令！"));
                return;
            }

            try {
                messageManager.loadMessages(); // 先加载消息文件
                configManager.loadConfig();   // 再加载配置文件
                source.sendMessage(messageManager.toComponent(messageManager.getMessageOrDefault("reload-success", "&a配置已成功重载！")));
            } catch (Exception e) {
                source.sendMessage(messageManager.toComponent(String.format(
                        messageManager.getMessageOrDefault("reload-failed", "&c重载配置时发生错误: {}"),
                        e.getMessage()
                )));
                logger.error("重载配置失败", e);
            }
        } else {
            source.sendMessage(messageManager.toComponent(messageManager.getMessageOrDefault("command-usage", "&e用法: /forcehosts reload - 重新加载配置")));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length == 0 && invocation.source().hasPermission("forcehosts.reload")) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("forcehosts.reload");
    }
}