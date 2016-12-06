package com.namelessmc.namelessplugin.spigot;


import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.namelessmc.namelessplugin.spigot.commands.GetUserCommand;
import com.namelessmc.namelessplugin.spigot.commands.RegisterCommand;
import com.namelessmc.namelessplugin.spigot.commands.ReportCommand;
import com.namelessmc.namelessplugin.spigot.mcstats.Metrics;
import com.namelessmc.namelessplugin.spigot.player.PlayerEventListener;

import net.milkbowl.vault.permission.Permission;

public class NamelessPlugin extends JavaPlugin {
	/*
	 *  Colors you can use to the console :)
	 */
	public final String COLOR_BLACK = "\u001B[30m";
	public final String COLOR_RED = "\u001B[31m";
	public final String COLOR_GREEN = "\u001B[32m";
	public final String COLOR_CYAN = "\u001B[36m";
	public final String COLOR_YELLOW = "\u001B[33m";
	public final String COLOR_BLUE = "\u001B[34m";
	public final String COLOR_PURPLE = "\u001B[35m";
	public final String COLOR_WHITE = "\u001B[37m";
	// Must use this after writing a line. 
	public final String COLOR_RESET = "\u001B[0m";
	
	/*
	 * Metrics
	 */
	Metrics metrics;
	
	
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
	private Permission permissions = null;
	
	/*
	 *  Enable reports?
	 */
	private boolean useReports = false;
	
	/*
	 *  Is the plugin disabled?
	 */
	private boolean isDisabled = false;
	
	/*
	 *  NameLessMC permission string.
	 */
	
	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";
	
	/*
	 *  OnEnable method
	 */
	@Override
	public void onEnable(){
		// Initialise config
		initConfig();
		
		if(!isDisabled){
			// Check Vault
			detectVault();
			
			registerListeners();
		}
	}
	
	/*
	 *  OnDisable method
	 */
	@Override
	public void onDisable(){
	}
	
	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
		// Register McStats
		try {
            metrics = new Metrics(this);
            metrics.start();
            getLogger().info(COLOR_CYAN + "Metrics Started!" + COLOR_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        } 
		
		// Register commands
		this.getCommand("register").setExecutor(new RegisterCommand(this));
		this.getCommand("getuser").setExecutor(new GetUserCommand(this));
		
		if(useReports){
			this.getCommand("report").setExecutor(new ReportCommand(this));
		}
		
		// Register events
		this.getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
	}
	
	/*
	 * Check if Vault is Activated
	 */
	public void detectVault(){
				if(getServer().getPluginManager().getPlugin("Vault") != null){
					// Installed
					useVault = true;
					initPermissions();
				} else {
					getLogger().info(COLOR_RED + "Couldn't detect Vault, disabling NamelessMC group synchronisation." + COLOR_RESET);
				}
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
				
				isDisabled = true;
				
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
				
				// Use the report system?
				if(yamlConfigFile.getBoolean("enable-reports"))
					useReports = true;
					
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
