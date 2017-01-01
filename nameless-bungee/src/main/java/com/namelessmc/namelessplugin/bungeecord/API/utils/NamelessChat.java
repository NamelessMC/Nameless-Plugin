package com.namelessmc.namelessplugin.bungeecord.API.utils;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;

public class NamelessChat {

	NamelessPlugin plugin;
	Configuration messageConfig;

	public NamelessChat(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public TextComponent sendClickableMessage(String message, ClickEvent.Action click, String actionText,
			HoverEvent.Action hover, String hoverText) {
		messageConfig = plugin.getAPI().getConfigs().getMessageConfig();
		TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
		msg.setClickEvent(new ClickEvent(click, actionText));
		msg.setHoverEvent(new HoverEvent(hover,
				new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
		return msg;
	}

	public BaseComponent[] convertColors(String message) {
		return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
	}

	public String getMessage(NamelessMessages message) {
		Configuration messageConfig = plugin.getAPI().getConfigs().getMessageConfig();
		return messageConfig.getString(message.toString());
	}
}
