package com.namelessmc.plugin.NamelessSpigot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bukkit.configuration.file.YamlConfiguration;

public enum Config {
	
	MAIN("config.yml", true, false),
	//PLAYER_INFO("player-data.yml", false, true),
	MESSAGES("messages.yml", true, false),
	COMMANDS("commands.yml", true, false),
	
	;
	
	private String fileName;
	private boolean copyFromJar;
	private boolean autoSave;
	
	private YamlConfiguration configuration;
	private File file;
	
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
			File file = config.getFile();
			if (!file.exists()) {
				if (config.copyFromJar) {
					try (InputStream in = plugin.getResource(config.fileName)) {
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
	
	public File getFile() {
		if (file == null) {
			file = new File(NamelessPlugin.getInstance().getDataFolder(), this.fileName);
			return file;
		} else {
			return file;
		}
	}
	
	public YamlConfiguration getConfig() {
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
		this.configuration = YamlConfiguration.loadConfiguration(getFile());
	}
	
	public void saveConfig() throws IOException {
		if (this.configuration != null) {
			configuration.save(getFile());
		}
	}

}