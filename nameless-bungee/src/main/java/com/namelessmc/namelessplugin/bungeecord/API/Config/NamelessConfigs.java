package com.namelessmc.namelessplugin.bungeecord.API.Config;

import java.io.File;
import java.io.IOException;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class NamelessConfigs {

    NamelessPlugin plugin;
	
	private Configuration config;
	private Configuration playerDataConfig;
	private Configuration permissionConfig;
	private Configuration messageConfig;
	
	public NamelessConfigs(NamelessPlugin plugin){
		this.plugin = plugin;
	}
	
	public Configuration getConfig(){
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "Config.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return config;
	}
	
	public Configuration getPlayerDataConfig(){
		try {
			playerDataConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "PlayersData.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return playerDataConfig;
	}
	
	public Configuration getGroupSyncPermissionsConfig(){
		try {
			permissionConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "GroupSyncPermissions.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return permissionConfig;
	}
	
	public Configuration getMessageConfig(){
		try {
			messageConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "Messages.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return messageConfig;
	}
	
	public boolean contains(Configuration file, String contain){
		return file.get(contain, null) != null;
	}
	
	public NamelessPluginInitFiles initFile(){
		NamelessPluginInitFiles init = new NamelessPluginInitFiles(plugin);
		return init;
		
	}
	
}
