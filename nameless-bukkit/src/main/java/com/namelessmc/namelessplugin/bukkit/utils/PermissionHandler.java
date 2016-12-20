package com.namelessmc.namelessplugin.bukkit.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.bukkit.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;

public class PermissionHandler {

	NamelessPlugin plugin;

	File config;

	YamlConfiguration loader;

	public YamlConfiguration getConfig(){
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
		InputStream defaultConfig = plugin.getClass().getClassLoader().getResourceAsStream("permissions.yml");

		plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Loading Group Synchronization..."));

		if(!config.exists()){
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Creating Permissions file..."));
			config.createNewFile();
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		loader = YamlConfiguration.loadConfiguration(config);
	}

}
