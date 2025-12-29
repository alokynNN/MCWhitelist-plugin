package com.mcwhitelist.auth;

import com.mcwhitelist.auth.api.ApiClient;
import com.mcwhitelist.auth.api.HeartbeatTask;
import com.mcwhitelist.auth.api.PlayerCheckTask;
import com.mcwhitelist.auth.commands.McWhitelistCommand;
import com.mcwhitelist.auth.config.ConfigManager;
import com.mcwhitelist.auth.listeners.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public class McWhitelistAuth extends JavaPlugin {
    
    private static McWhitelistAuth instance;
    private ConfigManager configManager;
    private ApiClient apiClient;
    private HeartbeatTask heartbeatTask;
    private PlayerCheckTask playerCheckTask;
    
    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        configManager = new ConfigManager(this);
        apiClient = new ApiClient(this);
        
        getCommand("mcwhitelist").setExecutor(new McWhitelistCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        if (configManager.isInitialized()) {
            startHeartbeat();
            startPlayerCheck();
        }
    }
    
    @Override
    public void onDisable() {
        if (heartbeatTask != null) {
            heartbeatTask.stop();
        }
        
        if (playerCheckTask != null) {
            playerCheckTask.stop();
        }
        
        if (configManager.isInitialized()) {
            apiClient.sendHeartbeat("offline", 0);
        }
    }
    
    public void startHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.stop();
        }
        
        if (configManager.isHeartbeatEnabled() && configManager.isInitialized()) {
            heartbeatTask = new HeartbeatTask(this);
            heartbeatTask.start();
        }
    }
    
    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.stop();
            heartbeatTask = null;
        }
        
        stopPlayerCheck();
    }
    
    public void startPlayerCheck() {
        if (playerCheckTask != null) {
            playerCheckTask.stop();
        }
        
        if (configManager.isPlayerCheckEnabled() && 
            configManager.isWhitelistEnabled() && 
            configManager.isInitialized()) {
            playerCheckTask = new PlayerCheckTask(this);
            playerCheckTask.start();
        }
    }
    
    public void stopPlayerCheck() {
        if (playerCheckTask != null) {
            playerCheckTask.stop();
            playerCheckTask = null;
        }
    }
    
    public static McWhitelistAuth getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public ApiClient getApiClient() {
        return apiClient;
    }
}