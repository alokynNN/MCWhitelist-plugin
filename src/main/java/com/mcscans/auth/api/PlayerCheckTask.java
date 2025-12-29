package com.mcwhitelist.auth.api;

import com.mcwhitelist.auth.McWhitelistAuth;
import com.mcwhitelist.auth.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PlayerCheckTask {
    
    private final McWhitelistAuth plugin;
    private BukkitTask task;
    
    public PlayerCheckTask(McWhitelistAuth plugin) {
        this.plugin = plugin;
    }
    
    public void start() {
        int intervalSeconds = plugin.getConfigManager().getPlayerCheckInterval();
        int intervalTicks = intervalSeconds * 20;
        
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (!plugin.getConfigManager().isInitialized() ||
                    !plugin.getConfigManager().isPlayerCheckEnabled() ||
                    !plugin.getConfigManager().isWhitelistEnabled()) {
                    return;
                }
                
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
        }, intervalTicks, intervalTicks);
    }
    
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}