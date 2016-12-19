package com.namelessmc.namelessplugin.spigot.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

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
		loader = YamlConfiguration.loadConfiguration(config);
		InputStream defaultConfig = getClass().getClassLoader().getResourceAsStream("permission.yml");

		if(!config.exists()){
			config.createNewFile();
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		} else {

		}
	}

}
