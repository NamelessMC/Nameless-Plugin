package com.namelessmc.namelessplugin.spigot.API.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;

public class NamelessPluginInitFiles {

	NamelessPlugin plugin;
	
	File configFile;
	File permissionConfig;
	File messageConfig;
	File commandsConfig;
	
	public NamelessPluginInitFiles(NamelessPlugin plugin){
		this.plugin = plugin;
		
		createDirs();
		initConfig();
		initCommands();
		initMessages();
		
		configFile = new File(plugin.getDataFolder() + File.separator + "Config.yml");
		YamlConfiguration yamlConfigFile;
		yamlConfigFile = YamlConfiguration.loadConfiguration(configFile);
		
		//Use group & username synchronization
		if(yamlConfigFile.getBoolean("update-username")){
			initPlayersData();
		}
		
		if(yamlConfigFile.getBoolean("group-synchronization")){
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
				try (InputStream in = plugin.getResource("Config.yml")) {
					// NamelessConfigs doesn't exist, create one now...
					plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreating NamelessMC configuration file...");
                    Files.copy(in, file.toPath());

                    plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_WARNING, "&4NamelessMC needs configuring, disabling features...");
                    plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_WARNING, "&4Please Configure NamelessMC config.yml!");

                } catch (IOException e) {
                    e.printStackTrace();
                }

			} else {
				configFile = new File(plugin.getDataFolder() + File.separator + "Config.yml");
				YamlConfiguration yamlConfigFile;
				yamlConfigFile = YamlConfiguration.loadConfiguration(configFile);
				
				// Exists already, load it
				plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoading NamelessMC configuration file...");

				plugin.setAPIUrl(yamlConfigFile.getString("api-url"));

				if(plugin.getAPIUrl().isEmpty()){
					// API URL not set
					 plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_WARNING, "&4No API URL set in the NamelessMC configuration, disabling features.");
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
	    plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoading Players Data File...");
		if(!iFile.exists()){
			try {
				iFile.createNewFile();
				plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Players Data File!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded Players Data File!");
		}
	}
	
	/*
	 * Initialize the Permissions NamelessConfigs.
	 */
	public void initPermissionHandler() {
		
		permissionConfig = new File(plugin.getDataFolder(), "GroupSyncPermissions.yml");

		plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoading Group Synchronization...");

		if(!permissionConfig.exists()){
			plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreating Group Sync Permissions file...");
			try(InputStream pConfig = plugin.getClass().getClassLoader().getResourceAsStream("GroupSyncPermissions.yml")) {
				plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Group Sync Permissions file!");
				Files.copy(pConfig, permissionConfig.getAbsoluteFile().toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded Group Sync Permissions file!");
		}
	}
	
	/*
	 * Initialize the Permissions NamelessConfigs.
	 */
	public void initMessages() {
		messageConfig = new File(plugin.getDataFolder(), "Messages.yml");

		plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoading Messages configuration...");

		if(!messageConfig.exists()){
			plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreating Messages file...");
			try(InputStream mConfig = plugin.getClass().getClassLoader().getResourceAsStream("Messages.yml");) {
				
				plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Messages file!");
				Files.copy(mConfig, messageConfig.getAbsoluteFile().toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded Messages file!");
		}

	}
	
	/*
	 * Initialize the Commands NamelessConfigs.
	 */
	public void initCommands() {
		commandsConfig = new File(plugin.getDataFolder(), "Commands.yml");

		plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoading Commands configuration...");

		if(!commandsConfig.exists()){
			plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreating Commands file...");
			try(InputStream cConfig = plugin.getClass().getClassLoader().getResourceAsStream("Commands.yml");) {
				
				plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Commands file!");
				Files.copy(cConfig, commandsConfig.getAbsoluteFile().toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			plugin.getAPI().getChat().sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded The Commands!");
		}

	}
}
