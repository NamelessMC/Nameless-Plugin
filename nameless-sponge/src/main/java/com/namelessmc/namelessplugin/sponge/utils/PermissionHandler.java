package com.namelessmc.namelessplugin.sponge.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.namelessmc.namelessplugin.sponge.NamelessPlugin;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

public class PermissionHandler {

	NamelessPlugin plugin;

	private File config;
	private YAMLConfigurationLoader loader;
	private ConfigurationNode configNode;

	public ConfigurationNode getConfig(){
		return configNode;
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
		config = new File(new File("config", "NamelessPlugin"), "permissions.yml");
		loader = YAMLConfigurationLoader.builder().setPath(config.toPath()).build();
		InputStream defaultConfig = plugin.getClass().getClassLoader().getResourceAsStream("permissions.yml");

		plugin.getLogger().info(Text.of(TextColors.BLUE, "Loading Group Synchronization...").toPlain());

		if(!config.exists()){
			plugin.getLogger().info(Text.of(TextColors.BLUE, "Creating Permissions file...").toPlain());
			config.createNewFile();
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			configNode = loader.load();
		} else {
			configNode = loader.load();
		}
	}

}
