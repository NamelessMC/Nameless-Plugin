package com.namelessmc.plugin.NamelessSpigot;

import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

public class Chat {

	public static net.md_5.bungee.api.chat.TextComponent sendClickableMessage(String message, net.md_5.bungee.api.chat.ClickEvent.Action click, String value,
			net.md_5.bungee.api.chat.HoverEvent.Action hover, String hoverText) {
		if (NamelessPlugin.getInstance().isSpigot()) {
			net.md_5.bungee.api.chat.TextComponent msg = new net.md_5.bungee.api.chat.TextComponent(convertColors(message));
			msg.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(click, value));
			msg.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(hover,
					new net.md_5.bungee.api.chat.ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
			return msg;
		} else {
			return null;
		}
}

	public static String convertColors(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static void log(Level level, String message) {
		NamelessPlugin.getInstance().getLogger().log(level, convertColors(message));
	}

}