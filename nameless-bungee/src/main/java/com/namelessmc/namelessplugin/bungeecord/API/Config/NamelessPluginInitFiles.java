package com.namelessmc.namelessplugin.bungeecord.API.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class NamelessPluginInitFiles {

	NamelessPlugin plugin;
	
	Configuration config;
	File permissionConfig;
	File messageConfig;
	
	public NamelessPluginInitFiles(NamelessPlugin plugin){
		this.plugin = plugin;
		
		createDirs();
		initConfig();
		initPlayersData();
		initMessages();
		
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "Config.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Use group synchronization
		if(config.getBoolean("group-synchronization")){
			initPermissionHandler();
		}
	}
	
	private void createDirs(){
		try{
			if(!plugin.getDataFolder().exists()){
				// Folder within plugins doesn't exist, create one now...
				plugin.getDataFolder().mkdirs();
			}	
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 *  Initialise configuration
	 */
	private void initConfig(){
		// Check config exists, if not create one
		try {

			File file = new File(plugin.getDataFolder(), "Config.yml");
			
			if(!file.exists()){
				try (InputStream in = plugin.getResourceAsStream("Config.yml")) {
					// NamelessConfigs doesn't exist, create one now...
					plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Creating NamelessMC configuration file..."));
                    Files.copy(in, file.toPath());

					plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4NamelessMC needs configuring, disabling features..."));
					plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4Please Configure NamelessMC config.yml!"));
					plugin.setHasSetUrl(false);

                } catch (IOException e) {
                    e.printStackTrace();
                }

			} else {
				config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "Config.yml"));

				// Exists already, load it
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Loading NamelessMC configuration file..."));

				plugin.setAPIUrl(config.getString("api-url"));

				if(plugin.getAPIUrl().isEmpty()){
					// API URL not set
					plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4No API URL set in the NamelessMC configuration, disabling features."));
					plugin.setHasSetUrl(false);
				}else{
					plugin.setHasSetUrl(true);
				}
			}

		} catch(Exception e){
			// Exception generated
			e.printStackTrace();
		}
	}

	/*
	 * Initialise the Player Info File
	 */
	private void initPlayersData() {
	    File iFile = new File(plugin.getDataFolder() + File.separator + "PlayersData.yml");
	    plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Loading Players Data File..."));
		if(!iFile.exists()){
			try {
				iFile.createNewFile();
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Created Players Data File!"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Loaded Players Data File!"));
		}
	}
	
	/*
	 * Initialize the Permissions NamelessConfigs.
	 */
	public void initPermissionHandler() {
		
		permissionConfig = new File(plugin.getDataFolder(), "GroupSyncPermissions.yml");

		plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Loading Group Synchronization..."));

		if(!permissionConfig.exists()){
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Creating Group Sync Permissions file..."));
			try(InputStream pConfig = plugin.getClass().getClassLoader().getResourceAsStream("GroupSyncPermissions.yml")) {
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Created Group Sync Permissions file!"));
				Files.copy(pConfig, permissionConfig.getAbsoluteFile().toPath());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Loaded Group Sync Permissions file!"));
		}
	}
	
	/*
	 * Initialize the Permissions NamelessConfigs.
	 */
	public void initMessages() {
		messageConfig = new File(plugin.getDataFolder(), "Messages.yml");

		plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Loading Messages configuration..."));

		if(!messageConfig.exists()){
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Creating Messages file..."));
			try(InputStream mConfig = plugin.getClass().getClassLoader().getResourceAsStream("Messages.yml");) {
				
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Created Messages file!"));
				Files.copy(mConfig, messageConfig.getAbsoluteFile().toPath());
				//Files.copy(defaultConfig, messageConfig.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Loaded Messages file!"));
		}

	}
}
