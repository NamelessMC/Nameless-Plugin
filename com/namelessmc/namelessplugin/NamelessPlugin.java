package com.namelessmc.namelessplugin;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

public class NamelessPlugin extends JavaPlugin {
	/*
	 *  API URL
	 */
	private String apiURL = "";
	
	/*
	 *  Instance of plugin
	 */
	static NamelessPlugin pluginInstance;
	
	/*
	 *  OnEnable method
	 */
	@Override
	public void onEnable(){
		// Initialise config
		initConfig();
		
		// Set instance
		pluginInstance = this;
		
		// Register commands
		this.getCommand("register").setExecutor(new RegisterCommand());
	}
	
	/*
	 *  OnDisable method
	 */
	@Override
	public void onDisable(){
		
	}
	
	/*
	 *  Initialise configuration
	 */
	private void initConfig(){
		// Check config exists, if not create one
		try {
			if(!getDataFolder().exists()){
				// Folder within plugins doesn't exist, create one now...
				getDataFolder().mkdirs();
			}
			
			File file = new File(getDataFolder(), "config.yml");
			
			if(!file.exists()){
				// Config doesn't exist, create one now...
				getLogger().info("Creating Nameless Plugin configuration file...");
				this.saveDefaultConfig();
				
				getLogger().info("Nameless Plugin needs configuring, disabling...");
				
				// Disable plugin
				getServer().getPluginManager().disablePlugin(this);
				
			} else {
				// Exists already, load it
				getLogger().info("Loading Nameless Plugin configuration file...");
				
				apiURL = this.getConfig().getString("api-url");
				if(apiURL.isEmpty()){
					// API URL not set
					getLogger().info("No API URL set in the Nameless Plugin configuration, disabling...");
					getServer().getPluginManager().disablePlugin(this);
				}
			}
			
		} catch(Exception e){
			// Exception generated
			e.printStackTrace();
		}
	}
	
	/*
	 *  Gets API URL
	 */
	public String getAPIUrl(){
		return apiURL;
	}
	
	
}
