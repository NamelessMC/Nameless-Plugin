package com.namelessmc.namelessplugin.spigot;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.namelessmc.namelessplugin.spigot.commands.GetNotificationsCommand;
import com.namelessmc.namelessplugin.spigot.commands.GetUserCommand;
import com.namelessmc.namelessplugin.spigot.commands.RegisterCommand;
import com.namelessmc.namelessplugin.spigot.commands.ReportCommand;
import com.namelessmc.namelessplugin.spigot.commands.SetGroupCommand;
import com.namelessmc.namelessplugin.spigot.mcstats.Metrics;
import com.namelessmc.namelessplugin.spigot.player.PlayerEventListener;

import net.milkbowl.vault.permission.Permission;

public class NamelessPlugin extends JavaPlugin {
	
	/*
	 * Metrics
	 */
	Metrics metrics;

	/*
	 *  API URL
	 */
	private String apiURL = "";

	/*
	 *  Vault Integration
	 */
	private boolean useVault = false;

	/*
	 *  Vault Permissions
	 */
	private Permission permissions = null;

	/*
	 *  Groups Support 
	 */
	@SuppressWarnings("unused")
	private boolean useGroups = false;

	/*
	 *  Enable reports?
	 */
	private boolean useReports = false;

	/*
	 *  Is the plugin disabled?
	 */
	private boolean isDisabled = false;

	/*
	 *  NamelessMC permissions strings.
	 */
	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";

	/*
	 *  OnEnable method
	 */
	@Override
	public void onEnable(){
		// Initialise  Files
		initConfig();
		initPlayerInfoFile();

		if(!isDisabled){
			// Check Vault
			detectVault();
			registerListeners();
		}
	}

	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
		// Register McStats
		try {
			metrics = new Metrics(this);
			metrics.start();
			getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Metrics Started!"));
		} catch (IOException e) {
			e.printStackTrace();
		} 

		// Register commands
		getCommand("register").setExecutor(new RegisterCommand(this));
		getCommand("getuser").setExecutor(new GetUserCommand(this));
		getCommand("getnotifications").setExecutor(new GetNotificationsCommand(this));
		getCommand("setgroup").setExecutor(new SetGroupCommand(this));

		if(useReports){
			getCommand("report").setExecutor(new ReportCommand(this));
		}

		// Register events
		getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
	}

	/*
	 * Check if Vault is Activated
	 */
	public void detectVault(){
				if(getServer().getPluginManager().getPlugin("Vault") != null){
					// Enable Vault integration and setup Permissions.
					useVault = true;
					initPermissions();
					// Check if the permissions plugin has groups.
					if(permissions.hasGroupSupport()){
						useGroups = true;
					} else {
						getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4Permissions plugin does NOT support groups! Disabling NamelessMC group synchronisation."));
						useGroups = false;
					}
				} else {
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4Couldn't detect Vault, disabling NamelessMC Vault integration."));
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
				getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Creating NamelessMC configuration file..."));
				this.saveDefaultConfig();

				getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4NamelessMC needs configuring, disabling..."));

				// Disable plugin
				getServer().getPluginManager().disablePlugin(this);

				isDisabled = true;

			} else {
				// Better way of loading config file, no need to reload.
				File configFile = new File(getDataFolder() + File.separator + "/config.yml");
				YamlConfiguration yamlConfigFile;
				yamlConfigFile = YamlConfiguration.loadConfiguration(configFile);

				// Exists already, load it
				getLogger().info(ChatColor.translateAlternateColorCodes('&', "&2Loading NamelessMC configuration file..."));

				apiURL = yamlConfigFile.getString("api-url");
				if(apiURL.isEmpty()){
					// API URL not set
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4No API URL set in the NamelessMC configuration, disabling..."));
					getServer().getPluginManager().disablePlugin(this);
				}

				// Use the report system?
				if(yamlConfigFile.getString("enable-reports").equals("true"))
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
	 * Initialise The Player Info File
	 */
	private void initPlayerInfoFile() {
	    File iFile = new File(this.getDataFolder() + File.separator + "playersInformation.yml");
		if(!iFile.exists()){
			try {
				iFile.createNewFile();
				getLogger().info(ChatColor.translateAlternateColorCodes('&', "&2Created players information File."));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 *  Update Username on Login
	 */
	public void userCheck(Player player){
		// Check if user does NOT contain information in the Players Information file. 
		// If so, add him.
	    File iFile = new File(this.getDataFolder() + File.separator + "playersInformation.yml");
    	YamlConfiguration yFile;
		yFile = YamlConfiguration.loadConfiguration(iFile);
		if(!yFile.contains(player.getUniqueId().toString())){
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&a" + player.getName() + " &cDoes not contain in the Player Information File.."));
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Adding &a" + player.getName() + " &2to the Player Information File."));
			yFile.addDefault(player.getUniqueId().toString() + ".Username", player.getName());
			yFile.options().copyDefaults(true);
			try {
				yFile.save(iFile);
				getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Added &a" + player.getName() + " &2to the Player Information File."));
			} catch (IOException e) {
				getLogger().info(ChatColor.translateAlternateColorCodes('&',"&cCould not add &a" + player.getName() + " &2to the Player Information File!"));
				e.printStackTrace();
			}
		}
		// Check if user has changed Username
		// If so, change the username in the Players Information File. (NOT COMPLETED)
		// And change the username on the website.
		else if(yFile.getString(player.getUniqueId() + ".Username") !=  player.getName()){
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&cDetected that&a" + player.getName() + " &2Has changed his/her username!"));
			getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changing &a" + player.getName() + "s &2Username."));

			String previousUsername = yFile.get(player.getUniqueId() + ".Username").toString();
			String newUsername = player.getName();
			yFile.addDefault(player.getUniqueId() + ".PreviousUsername", previousUsername);
			yFile.set(player.getUniqueId() + ".Username", newUsername);
			yFile.options().copyDefaults(true);
			try {
				yFile.save(iFile);
				getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changed &a" + player.getName() + "s &2Username in the Player Information File."));
			} catch (IOException e) {
				getLogger().info(ChatColor.translateAlternateColorCodes('&',"&c Could not change &a" + player.getName() + "s &2Username in the Player Information File."));
				e.printStackTrace();
			}

			// Changing username on Website here.
			// Comming in a bit.
		}
	}
}