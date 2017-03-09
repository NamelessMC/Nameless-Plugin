package com.namelessmc.namelessplugin.spigot.API.Config;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

public class NamelessConfigs {

    NamelessPlugin plugin;
	
	private File configFile;
	private File playerDataConfigFile;
	private File permissionConfigFile;
	private File messageConfigFile;
	private File commandsConfigFile;
	
	private YamlConfiguration yamlConfigFile;
	private YamlConfiguration yamlPlayerDataConfig;
	private YamlConfiguration yamlPermissionConfig;
	private YamlConfiguration yamlMessageConfig;
	private YamlConfiguration yamlCommandsConfig;
	
	public NamelessConfigs(NamelessPlugin plugin){
		this.plugin = plugin;
	}
	
	public YamlConfiguration getConfig(){
		configFile = new File(plugin.getDataFolder() + File.separator + "Config.yml");
		yamlConfigFile = YamlConfiguration.loadConfiguration(configFile);
		
		return yamlConfigFile;
	}
	
	public YamlConfiguration getPlayerDataConfig(){
		playerDataConfigFile = new File(plugin.getDataFolder() + File.separator + "PlayersData.yml");
		yamlPlayerDataConfig = YamlConfiguration.loadConfiguration(playerDataConfigFile);
		
		return yamlPlayerDataConfig;
	}
	
	public YamlConfiguration getGroupSyncPermissionsConfig(){
		permissionConfigFile = new File(plugin.getDataFolder() + File.separator + "GroupSyncPermissions.yml");
		yamlPermissionConfig = YamlConfiguration.loadConfiguration(permissionConfigFile);
		
		return yamlPermissionConfig;
	}
	
	public YamlConfiguration getMessageConfig(){
		messageConfigFile = new File(plugin.getDataFolder() + File.separator + "Messages.yml");
		yamlCommandsConfig = YamlConfiguration.loadConfiguration(messageConfigFile);
		
		return yamlCommandsConfig;
	}
	
	public YamlConfiguration getCommandsConfig(){
		commandsConfigFile = new File(plugin.getDataFolder() + File.separator + "Commands.yml");
		yamlMessageConfig = YamlConfiguration.loadConfiguration(commandsConfigFile);
		
		return yamlMessageConfig;
	}
	
	public NamelessPluginInitFiles initFile(){
		NamelessPluginInitFiles init = new NamelessPluginInitFiles(plugin);
		return init;
		
	}
	
}
