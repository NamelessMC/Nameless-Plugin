package com.namelessmc.NamelessBungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public enum Config {
	
	MAIN("Config.yml", true),
	PLAYER_INFO("PlayersData.yml", false),
	GROUP_SYNC_PERMISSIONS("GroupSyncPermissions.yml", true),
	MESSAGES("Messages.yml", true),
	COMMANDS("Commands.yml", true),
	
	;
	
	private String fileName;
	private boolean copyFromJar;
	
	private Configuration configuration;
	
	Config(String fileName, boolean copyFromJar){
		this.fileName = fileName;
		this.copyFromJar = copyFromJar;
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
	
	public void reloadConfig() throws IOException {
		this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(NamelessPlugin.getInstance().getDataFolder(), this.fileName));
	}

}