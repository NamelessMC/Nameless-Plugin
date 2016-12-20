package com.namelessmc.namelessplugin.sponge.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

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

	public Text getMessage(String path){
		return Text.of(TextSerializers.FORMATTING_CODE.replaceCodes(configNode.getNode(path).getString(), '&'));
	}

	public Text sendClickableMessage(String path, ClickAction<?> action){
		return Text.builder(TextSerializers.FORMATTING_CODE.replaceCodes(configNode.getNode(path).getString(), '&'))
				.onClick(action).build();
	}

	/*
	 * Initialize the Permissions Config.
	 */
	public void initMessages() throws Exception {
		config = new File(new File("config", "NamelessPlugin"), "messages.yml");
		InputStream defaultConfig = plugin.getClass().getClassLoader().getResourceAsStream("messages.yml");

		plugin.getLogger().info(Text.of(TextColors.BLUE, "Loading Messages configuration...").toPlain());

		if(!config.exists()){
			plugin.getLogger().info(Text.of(TextColors.BLUE, "Creating Messages file...").toPlain());
			config.createNewFile();
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			loader = YAMLConfigurationLoader.builder().setPath(config.toPath()).build();
			configNode = loader.load();
		} else {
			loader = YAMLConfigurationLoader.builder().setPath(config.toPath()).build();
			configNode = loader.load();
		}
	}

}