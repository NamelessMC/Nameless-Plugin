package com.namelessmc.plugin.NamelessBungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public enum Config {
	
	MAIN("config.yml", true, false),
	PLAYER_INFO("player-data.yml", false, true),
	GROUP_SYNC_PERMISSIONS("groupSyncPermissions.yml", true, false),
	MESSAGES("messages.yml", true, false),
	COMMANDS("commands.yml", true, false),
	
	;
	
	private String fileName;
	private boolean copyFromJar;
	private boolean autoSave;
	
	private Configuration configuration;
	
	Config(String fileName, boolean copyFromJar, boolean autoSave){
		this.fileName = fileName;
		this.copyFromJar = copyFromJar;
		this.autoSave = autoSave;
	}

	public static void initialize() throws IOException {
		NamelessPlugin plugin = NamelessPlugin.getInstance();
		
		// Create config directory
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}
		
		//Create config files if missing
		for (Config config : Config.values()) {
			File file = new File(plugin.getDataFolder(), config.fileName);
			if (!file.exists()) {
				if (config.copyFromJar) {
					try (InputStream in = plugin.getResourceAsStream(config.fileName)) {
						Files.copy(in, file.toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					file.createNewFile();
				}
			}
		}
	}
	
	public Configuration getConfig() {
		if (configuration == null) {
			try {
				reloadConfig();
				return configuration;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return configuration;
		}
	}
	
	public boolean autoSave() {
		return autoSave;
	}
	
	public void reloadConfig() throws IOException {
		this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(NamelessPlugin.getInstance().getDataFolder(), this.fileName));
	}
	
	public void saveConfig() throws IOException {
		if (this.configuration != null) {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.configuration, new File(NamelessPlugin.getInstance().getDataFolder(), this.fileName));
		}
	}

}