package com.mcwhitelist.auth.commands;

import com.mcwhitelist.auth.McWhitelistAuth;
import com.mcwhitelist.auth.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class McWhitelistCommand implements CommandExecutor, TabCompleter {
    
    private final McWhitelistAuth plugin;
    
    public McWhitelistCommand(McWhitelistAuth plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "init":
                if (!sender.hasPermission("mcscans.admin")) {
                    MessageUtil.send(sender, plugin.getConfigManager().getPrefix() + 
                        "&cYou don't have permission to use this command.");
                    return true;
                }

                if (args.length < 3) {
                    MessageUtil.send(sender, plugin.getConfigManager().getPrefix() + 
                        "&cUsage: /mcwhitelist init <server-uuid> <init-key>");
                    return true;
                }

                String serverUuid = args[1];
                String initKey = args[2];

                if (plugin.getConfigManager().isInitialized()) {
                    MessageUtil.send(sender, plugin.getConfigManager().getPrefix() + 
                        "&eServer is already initialized. Re-initializing will unlink the current connection...");
                }

                MessageUtil.send(sender, plugin.getConfigManager().getPrefix() + "&eInitializing plugin...");

                if (plugin.getApiClient().initializePlugin(serverUuid, initKey)) {
                    plugin.startHeartbeat();
                    plugin.startPlayerCheck();
                    
                    MessageUtil.send(sender, plugin.getConfigManager().getPrefix() + 
                        plugin.getConfigManager().getMessage("initialization-success"));
                    
                    checkAllOnlinePlayers();
                    
                } else {
                    MessageUtil.send(sender, plugin.getConfigManager().getPrefix() + 
                        plugin.getConfigManager().getMessage("initialization-failed"));
                }
                break;
                
            case "status":
                showStatus(sender);
                break;
                
            case "reload":
                plugin.getConfigManager().reload();
                plugin.stopHeartbeat();
                plugin.stopPlayerCheck();
                if (plugin.getConfigManager().isInitialized()) {
                    plugin.startHeartbeat();
                    plugin.startPlayerCheck();
                }
                MessageUtil.send(sender, plugin.getConfigManager().getPrefix() + 
                    plugin.getConfigManager().getMessage("reload-success"));
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void checkAllOnlinePlayers() {
        if (!plugin.getConfigManager().isWhitelistEnabled()) {
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("mcwhitelist.bypass")) {
                        continue;
                    }
                    
                    String kickReason = plugin.getApiClient().checkPlayerAccess(player.getName());
                    
                    if (kickReason != null) {
                        final String finalKickReason = kickReason;
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                player.kickPlayer(MessageUtil.colorize(finalKickReason));
                            }
                        });
                    }
                }
            }
        });
    }
    
    private void sendHelp(CommandSender sender) {
        MessageUtil.send(sender, "&8&m                                    ");
        MessageUtil.send(sender, "&b&lMcWhiteList &fPlugin Commands");
        MessageUtil.send(sender, "");
        MessageUtil.send(sender, "&e/mcwhitelist init <uuid> <key> &7- Initialize plugin");
        MessageUtil.send(sender, "&e/mcwhitelist status &7- Show plugin status");
        MessageUtil.send(sender, "&e/mcwhitelist reload &7- Reload configuration");
        MessageUtil.send(sender, "&8&m                                    ");
    }
    
    private void showStatus(CommandSender sender) {
        MessageUtil.send(sender, "&8&m                                    ");
        MessageUtil.send(sender, "&b&lMcWhitelist &fPlugin Status");
        MessageUtil.send(sender, "");
        
        if (plugin.getConfigManager().isInitialized()) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("status-initialized"));
            MessageUtil.send(sender, "&7Server UUID: &f" + plugin.getConfigManager().getServerUuid());
        } else {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("status-not-initialized"));
        }
        
        if (plugin.getConfigManager().isHeartbeatEnabled()) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("heartbeat-enabled"));
            MessageUtil.send(sender, "&7Interval: &f" + plugin.getConfigManager().getHeartbeatInterval() + "s");
        } else {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("heartbeat-disabled"));
        }
        
        if (plugin.getConfigManager().isWhitelistEnabled()) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("whitelist-enabled"));
        } else {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("whitelist-disabled"));
        }
        
        if (plugin.getConfigManager().isPlayerCheckEnabled()) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("player-check-enabled"));
            MessageUtil.send(sender, "&7Interval: &f" + plugin.getConfigManager().getPlayerCheckInterval() + "s");
        } else {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("player-check-disabled"));
        }
        
        MessageUtil.send(sender, "&7Online Players: &f" + plugin.getServer().getOnlinePlayers().size());
        MessageUtil.send(sender, "&8&m                                    ");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("init", "status", "reload"));
        }
        
        return completions;
    }
}