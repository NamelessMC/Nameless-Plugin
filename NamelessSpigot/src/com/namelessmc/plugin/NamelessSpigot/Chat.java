package com.namelessmc.plugin.NamelessSpigot;

import java.util.logging.Level;

import org.bukkit.ChatColor;

public class Chat {

	public static String convertColors(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static void log(Level level, String message) {
		NamelessPlugin.getInstance().getLogger().log(level, convertColors(message));
	}

}