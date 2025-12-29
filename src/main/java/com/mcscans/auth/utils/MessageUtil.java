package com.mcwhitelist.auth.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtil {
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
}