package com.namelessmc.namelessplugin.bungeecord.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

public class PermissionHandler {

	NamelessPlugin plugin;

	File config;

	Configuration loader;

	public Configuration getConfig(){
		return loader;
	}

	/*
	 * Constructor
	 */
	public PermissionHandler(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	/*
	 * Initialize the Permissions Config.
	 */
	public void initConfig() throws Exception {
		config = new File(plugin.getDataFolder(), "permissions.yml");
		loader = YamlConfiguration.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "permissions.yml"));
		InputStream defaultConfig = plugin.getClass().getClassLoader().getResourceAsStream("permission.yml");

		plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Loading Group Synchronization..."));

		if(!config.exists()){
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Creating Permissions file..."));
			config.createNewFile();
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
