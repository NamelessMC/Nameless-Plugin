package com.namelessmc.namelessplugin.spigot.API.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class NamelessChat {

	NamelessPlugin plugin;

	public NamelessChat(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public TextComponent sendClickableMessage(String message, ClickEvent.Action click, String value,
			HoverEvent.Action hover, String hoverText) {
		if (plugin.isSpigot()) {
			TextComponent msg = new TextComponent(convertColors(message));
			msg.setClickEvent(new ClickEvent(click, value));
			msg.setHoverEvent(new HoverEvent(hover,
					new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
			return msg;
		} else {
			return null;
		}
	}

	public static String convertColors(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static String getMessage(NamelessMessages message) {
		YamlConfiguration messageConfig = NamelessPlugin.getInstance().getAPI().getConfigManager().getMessageConfig();
		return messageConfig.getString(message.toString());
	}

	public static void sendToLog(NamelessMessages prefix, String message) {
		Bukkit.getConsoleSender().sendMessage(convertColors(prefix.toString()) + convertColors(message));
	}
}
