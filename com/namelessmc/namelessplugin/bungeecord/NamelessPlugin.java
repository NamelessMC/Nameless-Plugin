package com.namelessmc.namelessplugin.bungeecord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.namelessmc.namelessplugin.bungeecord.commands.GetUserCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.RegisterCommand;
import com.namelessmc.namelessplugin.bungeecord.mcstats.Metrics;
import com.namelessmc.namelessplugin.bungeecord.player.PlayerEventListener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

    /*
     * Bungeecord version made by IsS127
     */

public class NamelessPlugin extends Plugin {
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
            getLogger().info(COLOR_CYAN + "Metrics Started!" + COLOR_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        } 
		
		// Register commands
		getProxy().getPluginManager().registerCommand((Plugin)this, new RegisterCommand(this, "register"));
		getProxy().getPluginManager().registerCommand(this, new GetUserCommand(this, "getuser"));
		
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
					getLogger().info(COLOR_BLUE + "Creating NamelessMC configuration file..." + COLOR_RESET);
                    Files.copy(in, file.toPath());
					
					getLogger().info(COLOR_RED + "NamelessMC needs configuring, disabling features..." + COLOR_RESET);
					getLogger().info(COLOR_RED + "Please Configure NamelessMC config.yml!" + COLOR_RESET);
					hasSetUrl = false;
					
                } catch (IOException e) {
                    e.printStackTrace();
                }
				
			} else {
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				
				
				// Exists already, load it
				getLogger().info(COLOR_GREEN + "Loading NamelessMC configuration file..." + COLOR_RESET);
				
				apiURL = config.getString("api-url");
				
				if(apiURL.isEmpty()){
					// API URL not set
					getLogger().info(COLOR_RED + "No API URL set in the NamelessMC configuration, disabling features." + COLOR_RESET);
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
