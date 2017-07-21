package com.namelessmc.namelessplugin.bungeecord.API.Utils;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;

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

	public static BaseComponent[] convertColors(String message) {
		return TextComponent.fromLegacyText(convertColorsString(message));
	}

	public static String getMessage(NamelessMessages message) {
		Configuration messageConfig = NamelessPlugin.getInstance().getAPI().getConfigManager().getMessageConfig();
		return convertColorsString(messageConfig.getString(message.toString()));
	}

	public static void sendToLog(NamelessMessages prefix, String message) {
		ProxyServer.getInstance().getConsole()
				.sendMessage(convertColors(prefix.toString() + convertColorsString(message)));
	}

}