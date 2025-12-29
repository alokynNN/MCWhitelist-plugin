package com.mcwhitelist.auth.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mcwhitelist.auth.McWhitelistAuth;
import org.bukkit.Bukkit;
import com.mcwhitelist.auth.config.ConfigManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    
    private final McWhitelistAuth plugin;
    private final Gson gson;
    
    public ApiClient(McWhitelistAuth plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
    }
    
    public boolean initializePlugin(String serverUuid, String initKey) {
        try {
            String baseUrl = plugin.getConfigManager().getApiBaseUrl();
            URL url = new URL(baseUrl + "/servers/init");
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            Map<String, String> data = new HashMap<String, String>();
            data.put("server_uuid", serverUuid);
            data.put("init_key", initKey);
            
            String jsonData = gson.toJson(data);
            
            OutputStream os = conn.getOutputStream();
            byte[] input = jsonData.getBytes("UTF-8");
            os.write(input, 0, input.length);
            os.close();
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                plugin.getConfigManager().setServerUuid(serverUuid);
                plugin.getConfigManager().setInitKey(initKey);
                plugin.getConfigManager().setInitialized(true);
                return true;
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void sendHeartbeat(final String status, final int onlinePlayers) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    String serverUuid = plugin.getConfigManager().getServerUuid();
                    String baseUrl = plugin.getConfigManager().getApiBaseUrl();
                    URL url = new URL(baseUrl + "/servers/" + serverUuid + "/heartbeat");
                    
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("online_players", onlinePlayers);
                    data.put("max_players", Bukkit.getMaxPlayers());
                    data.put("status", status);
                    data.put("plugin_initialized", true);
                    
                    String jsonData = gson.toJson(data);
                    
                    OutputStream os = conn.getOutputStream();
                    byte[] input = jsonData.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                    os.close();
                    
                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode == 401 || responseCode == 403) {
                        plugin.getConfigManager().setInitialized(false);
                        plugin.stopHeartbeat();
                        
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                Bukkit.broadcastMessage("§c[McWhitelist] §ePlugin needs reinitialization! Contact server owner.");
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private String getKickMessage(String reason) {
        ConfigManager config = plugin.getConfigManager();
        
        switch (reason) {
            case "software_required":
                return config.getSoftwareRequiredMessage();
            case "not_whitelisted":
                return config.getKickMessage();
            case "user_not_found":
                return config.getUserNotFoundMessage();
            case "plugin_error":
                return config.getPluginErrorMessage();
            case "verification_failed":
                return config.getVerificationFailedMessage();
            default:
                return config.getKickMessage();
        }
    }

    public String checkPlayerAccess(String playerName) {
        try {
            String serverUuid = plugin.getConfigManager().getServerUuid();
            String baseUrl = plugin.getConfigManager().getApiBaseUrl();
            URL url = new URL(baseUrl + "/servers/" + serverUuid + "/check-whitelist");
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("minecraft_username", playerName);
            
            String jsonData = gson.toJson(data);
            
            OutputStream os = conn.getOutputStream();
            byte[] input = jsonData.getBytes("UTF-8");
            os.write(input, 0, input.length);
            os.close();
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                boolean whitelisted = jsonResponse.get("whitelisted").getAsBoolean();
                
                if (whitelisted) {
                    return null;
                } else {
                    String reason = jsonResponse.has("reason") ? jsonResponse.get("reason").getAsString() : "not_whitelisted";
                    return getKickMessage(reason);
                }
            } else {
                return getKickMessage("plugin_error");
            }
        } catch (Exception e) {
            return getKickMessage("plugin_error");
        }
    }
}