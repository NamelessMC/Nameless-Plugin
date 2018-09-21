package com.namelessmc.plugin.NamelessSpigot;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import xyz.derkades.derkutils.FileUtils;

public enum Config {
	
	MAIN("config.yml", true, false),
	//PLAYER_INFO("player-data.yml", false, true),
	MESSAGES("messages.yml", false, false),
	COMMANDS("commands.yml", true, false),
	
	;
	
	private String fileName;
	private boolean copyFromJar;
	private boolean autoSave;
	
	private FileConfiguration configuration;
	private File file;
	
	Config(String fileName, boolean copyFromJar, boolean autoSave){
		this.copyFromJar = copyFromJar;
		this.autoSave = autoSave;
		
		File dataFolder = NamelessPlugin.getInstance().getDataFolder();
		if (!dataFolder.exists()) dataFolder.mkdirs();
		this.file = new File(NamelessPlugin.getInstance().getDataFolder(), this.fileName);
	}

	public static void initialize() throws IOException {
		NamelessPlugin plugin = NamelessPlugin.getInstance();
		
		// Create config directory
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}
		
		//Create config files if missing
		for (Config config : Config.values()) {
			config.reload();
		}
		
		// Manually generate messages configuration file from enum values.
		// If the file already exists, the method will try to add any missing entries.
		Message.generateConfig(MESSAGES);
	}
	
	public FileConfiguration getConfig() {
		if (configuration == null) {
			reload();
		}
		
		return configuration;
	}
	
	public void setConfig(FileConfiguration config) {
		this.configuration = config;
	}
	
	public boolean autoSave() {
		return autoSave;
	}
	
	public void reload() {
		if (!file.exists()) {
			try {
				if (copyFromJar) {
					FileUtils.copyOutOfJar(Config.class, "/xyz/derkades/metadatasaver/default-config.yml", file);
				} else {
					file.createNewFile(); //Create blank file
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		this.configuration = YamlConfiguration.loadConfiguration(file);
	}
	
	public void save() {
		if (this.configuration != null) {
			try {
				configuration.save(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}