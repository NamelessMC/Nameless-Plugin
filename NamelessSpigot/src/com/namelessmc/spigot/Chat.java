package com.namelessmc.spigot;

import org.bukkit.ChatColor;

public class Chat {

	public static String convertColors(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

}