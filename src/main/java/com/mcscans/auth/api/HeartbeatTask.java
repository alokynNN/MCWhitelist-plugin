package com.mcwhitelist.auth.api;

import com.mcwhitelist.auth.McWhitelistAuth;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class HeartbeatTask {
    
    private final McWhitelistAuth plugin;
    private BukkitTask task;
    
    public HeartbeatTask(McWhitelistAuth plugin) {
        this.plugin = plugin;
    }
    
    public void start() {
        int interval = plugin.getConfigManager().getHeartbeatInterval() * 20;
        
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                int onlinePlayers = Bukkit.getOnlinePlayers().size();
                plugin.getApiClient().sendHeartbeat("online", onlinePlayers);
            }
        }, 0L, interval);
    }
    
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}