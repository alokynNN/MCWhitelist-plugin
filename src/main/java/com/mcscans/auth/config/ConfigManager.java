package com.mcwhitelist.auth.config;

import com.mcwhitelist.auth.McWhitelistAuth;

public class ConfigManager {
    
    private final McWhitelistAuth plugin;
    
    public ConfigManager(McWhitelistAuth plugin) {
        this.plugin = plugin;
    }
    
    public String getApiBaseUrl() {
        String url = plugin.getConfig().getString("api.base-url", "https://mcwhitelist.alokyn.com/api");
        
        if (!url.equals("https://mcwhitelist.alokyn.com/api") && !url.startsWith("https://")) {
            plugin.getLogger().warning("NOTICE: API URL has been manually modified. Only change this if you know what you're doing!");
        }
        
        return url;
    }
    
    public boolean isDebugEnabled() {
        return plugin.getConfig().getBoolean("debug", false);
    }
    
    public String getServerUuid() {
        return plugin.getConfig().getString("api.server-uuid", "");
    }
    
    public String getInitKey() {
        return plugin.getConfig().getString("api.init-key", "");
    }
    
    public boolean isInitialized() {
        return plugin.getConfig().getBoolean("initialized", false);
    }
    
    public boolean isHeartbeatEnabled() {
        return plugin.getConfig().getBoolean("heartbeat.enabled", true);
    }
    
    public int getHeartbeatInterval() {
        return plugin.getConfig().getInt("heartbeat.interval", 5);
    }
    
    public boolean isWhitelistEnabled() {
        return plugin.getConfig().getBoolean("whitelist.enabled", true);
    }
    
    public boolean isPlayerCheckEnabled() {
        return plugin.getConfig().getBoolean("player-check.enabled", true);
    }
    
    public int getPlayerCheckInterval() {
        return plugin.getConfig().getInt("player-check.interval", 5);
    }
    
    public String getKickMessage() {
        return plugin.getConfig().getString("whitelist.kick-message", 
            "&c[McWhitelist]\n\n&eYou are not whitelisted on this server!");
    }
    
    public String getSoftwareRequiredMessage() {
        return plugin.getConfig().getString("kick-messages.software-required",
            "&c[McWhitelist]\n\n&eYou must have McWhitelist software running!\n\n&7Download: &bmcwhitelist.com");
    }
    
    public String getUserNotFoundMessage() {
        return plugin.getConfig().getString("kick-messages.user-not-found",
            "&c[McWhitelist]\n\n&eAccount not found!\n&7Register at: &bmcwhitelist.com");
    }
    
    public String getPluginErrorMessage() {
        return plugin.getConfig().getString("kick-messages.plugin-error",
            "&c[McWhitelist]\n\n&eServer plugin needs reinitialization!\n&7Contact server administrator.");
    }
    
    public String getVerificationFailedMessage() {
        return plugin.getConfig().getString("kick-messages.verification-failed",
            "&c[McWhitelist]\n\n&eCould not verify access!\n&7Try again or contact server administrator.");
    }
    
    public String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, key);
    }
    
    public String getPrefix() {
        return getMessage("prefix");
    }
    
    public void setServerUuid(String uuid) {
        plugin.getConfig().set("api.server-uuid", uuid);
        save();
    }
    
    public void setInitKey(String key) {
        plugin.getConfig().set("api.init-key", key);
        save();
    }
    
    public void setInitialized(boolean initialized) {
        plugin.getConfig().set("initialized", initialized);
        save();
    }
    
    public void reload() {
        plugin.reloadConfig();
    }
    
    public void save() {
        plugin.saveConfig();
    }
}