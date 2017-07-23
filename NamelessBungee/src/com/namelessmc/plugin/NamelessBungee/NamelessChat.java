package com.namelessmc.NamelessBungee;

import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class NamelessChat {

	public static TextComponent sendClickableMessage(String message, ClickEvent.Action click, String value,
			HoverEvent.Action hover, String hoverText) {
		TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
		msg.setClickEvent(new ClickEvent(click, value));
		msg.setHoverEvent(new HoverEvent(hover, new ComponentBuilder(convertColorsString(hoverText)).create()));
		return msg;
	}

	public static String convertColorsString(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public static void log(Level level, String message) {
		NamelessPlugin.getInstance().getLogger().log(level, convertColorsString(message));
	}

}