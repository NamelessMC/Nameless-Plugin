package com.namelessmc.namelessplugin.bungeecord;

import java.io.IOException;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.commands.GetNotificationsCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.GetUserCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.RegisterCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.ReportCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.SetGroupCommand;
import com.namelessmc.namelessplugin.bungeecord.mcstats.Metrics;
import com.namelessmc.namelessplugin.bungeecord.player.PlayerEventListener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

/*
 *  Bungeecord Version by IsS127
 */

public class NamelessPlugin extends Plugin {

    /*
     * Plugin API
     */
	private NamelessAPI api; 
	
	/*
	 *  API URL
	 */
	private String apiURL = "";
	private boolean hasSetUrl = false;

	/*
	 *  NamelessMC permission string.
	 */

	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";

	/*
	 *  Metrics
	 */
	Metrics metrics;

	/*
	 *  OnEnable method
	 */
	@Override
	public void onEnable(){
		// Register the API
		api = new NamelessAPI(this);
		
		// Init config files.
		api.getConfigs().initFile();
	    
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

		// Register commands & listeners if url has been set
		if(hasSetUrl){
			getProxy().getPluginManager().registerCommand(this, new RegisterCommand(this, "register"));
			getProxy().getPluginManager().registerCommand(this, new GetUserCommand(this, "getuser"));
			getProxy().getPluginManager().registerCommand(this, new GetNotificationsCommand(this, "getnotifications"));
			getProxy().getPluginManager().registerCommand(this, new SetGroupCommand(this, "setgroup"));

			if (getAPI().getConfigs().getConfig().getBoolean("enable-reports")) {
				getProxy().getPluginManager().registerCommand(this, new ReportCommand(this, "report"));
			}
			
			// Register events
			getProxy().getPluginManager().registerListener(this, new PlayerEventListener(this));
		}
	}

	/*
	 * UnRegister Commands/Events
	 */
	public void unRegisterListeners(){
		// UnRegister commands
		getProxy().getPluginManager().unregisterCommand(new RegisterCommand(this, "register"));
		getProxy().getPluginManager().unregisterCommand(new GetUserCommand(this, "getuser"));
		if (getAPI().getConfigs().getConfig().getBoolean("enable-reports")) {
			getProxy().getPluginManager().unregisterCommand(new ReportCommand(this, "report"));
		}

		// UnRegister Listeners/Events
		getProxy().getPluginManager().unregisterListener(new PlayerEventListener(this));
	}
	
	/*
	 *  Get / Has / Set
	 */
	
	// Gets the website api url.
	public String getAPIUrl(){
		return apiURL;
	}

	// Gets the Plugin API
	public NamelessAPI getAPI(){
		return api;
	}
	
	// Checks if hasSetUrl
	public boolean hasSetUrl(){
		return hasSetUrl;
	}
	
	// Sets HasSetUrl
	public void setHasSetUrl(boolean value){
		hasSetUrl = value;
	}
	
	// Sets api url
	public void setAPIUrl(String value){
		apiURL = value;
	}
	
	/*
	 * End! 
	 */

}