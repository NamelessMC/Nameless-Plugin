package com.namelessmc.namelessplugin;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.permission.Permission;

public class NamelessPlugin extends JavaPlugin {
	/*
	 *  Colors you can use to the console :)
	 */
	public static final String COLOR_BLACK = "\u001B[30m";
	public static final String COLOR_RED = "\u001B[31m";
	public static final String COLOR_GREEN = "\u001B[32m";
	public static final String COLOR_CYAN = "\u001B[36m";
	public static final String COLOR_YELLOW = "\u001B[33m";
	public static final String COLOR_BLUE = "\u001B[34m";
	public static final String COLOR_PURPLE = "\u001B[35m";
	public static final String COLOR_WHITE = "\u001B[37m";
	// Must use this after writing a line. 
	public static final String COLOR_RESET = "\u001B[0m";
	
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
			getLogger().info(COLOR_RED + "Couldn't detect Vault, disabling NamelessMC group synchronisation." + COLOR_RESET);
		}
		
		
		// Register commands
		this.getCommand("register").setExecutor(new RegisterCommand(this));
		
		// Register events
		this.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
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
				getLogger().info(COLOR_BLUE + "Creating NamelessMC configuration file..." + COLOR_RESET);
				this.saveDefaultConfig();
				
				getLogger().info(COLOR_RED + "NamelessMC needs configuring, disabling..." + COLOR_RESET);
				
				// Disable plugin
				getServer().getPluginManager().disablePlugin(this);
				
			} else {
				// Better way of loading config file, no need to reload.
		    	File configFile = new File(getDataFolder() + File.separator + "/config.yml");
				YamlConfiguration yamlConfigFile;
				yamlConfigFile = YamlConfiguration.loadConfiguration(configFile);
				
				
				// Exists already, load it
				getLogger().info(COLOR_GREEN + "Loading NamelessMC configuration file..." + COLOR_RESET);
				
				apiURL = yamlConfigFile.getString("api-url");
				if(apiURL.isEmpty()){
					// API URL not set
					getLogger().info(COLOR_RED + "No API URL set in the NamelessMC configuration, disabling..." + COLOR_RESET);
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
