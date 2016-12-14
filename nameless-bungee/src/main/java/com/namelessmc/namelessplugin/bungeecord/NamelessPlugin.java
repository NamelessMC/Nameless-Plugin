package com.namelessmc.namelessplugin.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.namelessmc.namelessplugin.bungeecord.commands.GetUserCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.RegisterCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.ReportCommand;
import com.namelessmc.namelessplugin.bungeecord.mcstats.Metrics;
import com.namelessmc.namelessplugin.bungeecord.player.PlayerEventListener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/*
 *  Bungeecord Version by IsS127
 */

public class NamelessPlugin extends Plugin {
	
	/*
	 *  API URL
	 */
	private String apiURL = "";
	public boolean hasSetUrl = true;
	
	
	/*
	 *  NameLessMC permission string.
	 */
	
	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";
	
	/*
	 * Metrics
	 */
	Metrics metrics;
	
	/*
	 *  OnEnable method
	 */
	@Override
	public void onEnable(){
		// Initialise config
		initConfig();
		registerListeners();
	}
	
	/*
	 *  OnDisable method
	 */
	@Override
	public void onDisable(){
		unRegisterListeners();
	}
	
	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
		try {
            metrics = new Metrics(this);
            metrics.start();
            getLogger().info(ChatColor.translateAlternateColorCodes('&', "&3Metrics Started!"));
        } catch (IOException e) {
            e.printStackTrace();
        } 
		
		// Register commands
		getProxy().getPluginManager().registerCommand((Plugin)this, new RegisterCommand(this, "register"));
		getProxy().getPluginManager().registerCommand(this, new GetUserCommand(this, "getuser"));
		getProxy().getPluginManager().registerCommand((Plugin)this, new ReportCommand(this, "report"));
		
		// Register events
		getProxy().getPluginManager().registerListener(this, new PlayerEventListener(this));
	}
	
	/*
	 * UnRegister Commands/Events
	 */
	public void unRegisterListeners(){
		// UnRegister commands
		getProxy().getPluginManager().unregisterCommand(new RegisterCommand(this, "register"));
		getProxy().getPluginManager().unregisterCommand(new GetUserCommand(this, "getuser"));
		getProxy().getPluginManager().unregisterCommand(new ReportCommand(this, "report"));
		
		// UnRegister Listeners/Events
		getProxy().getPluginManager().unregisterListener(new PlayerEventListener(this));
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
				try (InputStream in = getResourceAsStream("config.yml")) {
					// Config doesn't exist, create one now...
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&1Creating NamelessMC configuration file..."));
                    Files.copy(in, file.toPath());
					
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4NamelessMC needs configuring, disabling features..."));
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4Please Configure NamelessMC config.yml!"));
					hasSetUrl = false;
					
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
			} else {
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				
				
				// Exists already, load it
				getLogger().info(ChatColor.translateAlternateColorCodes('&', "&2Loading NamelessMC configuration file..."));
				
				apiURL = config.getString("api-url");
				
				if(apiURL.isEmpty()){
					// API URL not set
					getLogger().info(ChatColor.translateAlternateColorCodes('&', "&4No API URL set in the NamelessMC configuration, disabling features."));
					hasSetUrl = false;
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
	 *  Update username/group on login
	 */
	public boolean loginCheck(ProxiedPlayer player){
		// Check when user last logged in, only update username and group if over x hours ago
		// TODO
		return true;
	}
}
