package com.namelessmc.namelessplugin.spigot.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class MessagesUtil {

	NamelessPlugin plugin;
	
	private File config;
	private YamlConfiguration loader;

	public MessagesUtil(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public String getMessage(String path){
		return ChatColor.translateAlternateColorCodes('&', loader.getString(path));
	}

	public BaseComponent sendClickableMessage(String path, Action click, String actionText){
		TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', loader.getString(path)));
		message.setClickEvent(new ClickEvent(click, actionText));
		return message;
	}

	/*
	 * Initialize the Permissions Config.
	 */
	public void initMessages() throws Exception {
		config = new File(plugin.getDataFolder(), "messages.yml");
		InputStream defaultConfig = plugin.getClass().getClassLoader().getResourceAsStream("messages.yml");

		plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Loading Messages configuration..."));

		if(!config.exists()){
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Creating Messages file..."));
			config.createNewFile();
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		loader = YamlConfiguration.loadConfiguration(config);
	}

}