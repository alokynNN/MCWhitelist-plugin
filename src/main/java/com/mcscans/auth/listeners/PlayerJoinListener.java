package com.mcwhitelist.auth.listeners;

import com.mcwhitelist.auth.McWhitelistAuth;
import com.mcwhitelist.auth.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerJoinListener implements Listener {
    
    private final McWhitelistAuth plugin;
    
    public PlayerJoinListener(McWhitelistAuth plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!plugin.getConfigManager().isWhitelistEnabled()) {
            return;
        }
        
        if (!plugin.getConfigManager().isInitialized()) {
            return;
        }
        
        if (event.getPlayer().hasPermission("mcwhitelist.bypass")) {
            return;
        }
        
        String playerName = event.getPlayer().getName();
        
        String kickReason = plugin.getApiClient().checkPlayerAccess(playerName);
        
        if (kickReason != null) {
            String kickMessage = MessageUtil.colorize(kickReason);
            event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, kickMessage);
        }
    }
}