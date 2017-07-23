package com.namelessmc.namelessplugin.spigot.API.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

public class NamelessChat {

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

	public static String getMessage(NamelessMessages message) {
		YamlConfiguration messageConfig = NamelessPlugin.getInstance().getAPI().getConfigManager().getMessageConfig();
		return convertColors(messageConfig.getString(message.toString()));
	}

	public static void sendToLog(NamelessMessages prefix, String message) {
		Bukkit.getConsoleSender().sendMessage(convertColors(prefix.toString()) + convertColors(message));
	}

}