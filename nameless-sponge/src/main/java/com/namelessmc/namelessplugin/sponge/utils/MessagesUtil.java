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

public class MessagesUtil {

	NamelessPlugin plugin;
	
	private File config;
	private YAMLConfigurationLoader loader;
	private ConfigurationNode configNode;

	public MessagesUtil(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public String getMessage(String path){
		return configNode.getNode(path).getString();
	}

	/*
	 * Initialize the Permissions Config.
	 */
	public void initMessages() throws Exception {
		config = new File(new File("config", "NamelessPlugin"), "messages.yml");
		loader = YAMLConfigurationLoader.builder().setPath(config.toPath()).build();
		InputStream defaultConfig = plugin.getClass().getClassLoader().getResourceAsStream("messages.yml");

		plugin.getLogger().info(Text.of(TextColors.BLUE, "Loading Messages configuration...").toPlain());

		if(!config.exists()){
			plugin.getLogger().info(Text.of(TextColors.BLUE, "Creating Messages file...").toPlain());
			config.createNewFile();
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			configNode = loader.load();
		} else {
			configNode = loader.load();
		}
	}

}