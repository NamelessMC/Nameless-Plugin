package com.namelessmc.namelessplugin;

import java.io.File;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.permission.Permission;

public class NamelessPlugin extends JavaPlugin {
	/*
	 *  API URL
	 */
	private String apiURL = "";
	
	/*
	 *  Is Vault integration enabled?
	 */
	private boolean useVault = false;
	
	/*
	 *  Vault permissions
	 */
	private static Permission permissions = null;
	
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
		
		// Check Vault
		if(getServer().getPluginManager().getPlugin("Vault") != null){
			// Installed
			useVault = true;
			initPermissions();
		} else {
			getLogger().info("Couldn't detect Vault, disabling NamelessMC group synchronisation.");
		}
		
		// Set instance
		pluginInstance = this;
		
		// Register commands
		this.getCommand("register").setExecutor(new RegisterCommand());
		
		// Register events
		getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
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
				getLogger().info("Creating NamelessMC configuration file...");
				this.saveDefaultConfig();
				
				getLogger().info("NamelessMC needs configuring, disabling...");
				
				// Disable plugin
				getServer().getPluginManager().disablePlugin(this);
				
			} else {
				// Exists already, load it
				getLogger().info("Loading NamelessMC configuration file...");
				
				apiURL = this.getConfig().getString("api-url");
				if(apiURL.isEmpty()){
					// API URL not set
					getLogger().info("No API URL set in the NamelessMC configuration, disabling...");
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
	
	/*
	 *  Initialise Vault permissions integration for group sync
	 */
	private boolean initPermissions(){
		if(useVault){
	        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
	        permissions = rsp.getProvider();
		}
		
		return permissions != null;
	}
	
	
	/*
	 *  Update username/group on login
	 */
	public boolean loginCheck(Player player){
		// Check when user last logged in, only update username and group if over x hours ago
		// TODO
		
		return true;
	}
	
}
