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
	YamlConfiguration messageConfig;

	public NamelessChat(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public TextComponent sendClickableMessage(String message, ClickEvent.Action click, String actionText,
			HoverEvent.Action hover, String hoverText) {
		if(plugin.isSpigot()){
			messageConfig = plugin.getAPI().getConfigs().getMessageConfig();
			TextComponent msg = new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
			msg.setClickEvent(new ClickEvent(click, actionText));
			msg.setHoverEvent(new HoverEvent(hover,
					new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));
			return msg;
		}else{
			return null;
		}
	}

	public String convertColors(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String getMessage(NamelessMessages message) {
		YamlConfiguration messageConfig = plugin.getAPI().getConfigs().getMessageConfig();
		return messageConfig.getString(message.toString());
	}
	
	public void sendToLog(NamelessMessages prefix, String message){
		Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',prefix.toString()) + ChatColor.translateAlternateColorCodes('&', message));
	}
}
