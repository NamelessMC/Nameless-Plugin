package com.namelessmc.plugin.NamelessBungee;

import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

public class Chat {

	public static String convertColorsString(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static void log(Level level, String message) {
		NamelessPlugin.getInstance().getLogger().log(level, convertColorsString(message));
	}

}