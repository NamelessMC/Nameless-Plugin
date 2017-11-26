package com.namelessmc.plugin.NamelessSponge;

import java.io.File;
import java.io.IOException;

import org.spongepowered.api.Game;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.util.MapFactories;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

public enum Config {
	
	MAIN("config.yml", true, false),
	GROUP_SYNC_PERMISSIONS("group-sync.yml", true, false),
	MESSAGES("messages.yml", true, false),
	COMMANDS("commands.yml", true, false),
	
	;
	
	private String fileName;
	private boolean copyFromJar;
	private boolean autoSave;
	
	private YAMLConfigurationLoader loader;
	private ConfigurationNode configuration;
	private File file;
	
	Config(String fileName, boolean copyFromJar, boolean autoSave){
		this.fileName = fileName;
		this.copyFromJar = copyFromJar;
		this.autoSave = autoSave;
	}

	public static void initialize() throws IOException {
		Game game = NamelessPlugin.getGame();
		
		if(!NamelessPlugin.getDirectory().toFile().exists()) {
			NamelessPlugin.getDirectory().toFile().mkdirs();
		}
		
		//Create config files if missing
		for (Config config : Config.values()) {
			File file = config.getFile();
			if (!file.exists()) {
				if (config.copyFromJar) {
					game.getAssetManager().getAsset(NamelessPlugin.getInstance(), config.fileName).orElse(null).copyToDirectory(NamelessPlugin.getDirectory());
				} else {
					file.createNewFile();
				}
			}
		}
	}
	
	public File getFile() {
		if (file == null) {
			file = new File(NamelessPlugin.getDirectory().toFile(), fileName);
			return file;
		} else {
			return file;
		}
	}
	
	public ConfigurationNode getConfig() {
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
	    loader = YAMLConfigurationLoader.builder().setFile(getFile())
	            .setDefaultOptions(ConfigurationOptions.defaults().setMapFactory(MapFactories.insertionOrdered()))
	            .build();
	    try {
			this.configuration = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveConfig() throws IOException {
		if (this.configuration != null) {
			loader.save(configuration);
		}
	}
	
	

}