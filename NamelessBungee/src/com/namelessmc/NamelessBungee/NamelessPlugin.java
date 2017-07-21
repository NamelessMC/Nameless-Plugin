package com.namelessmc.NamelessBungee;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.NamelessBungee.commands.CommandWithArgs;
import com.namelessmc.NamelessBungee.commands.GetNotificationsCommand;
import com.namelessmc.NamelessBungee.commands.GetUserCommand;
import com.namelessmc.NamelessBungee.commands.RegisterCommand;
import com.namelessmc.NamelessBungee.commands.ReportCommand;
import com.namelessmc.NamelessBungee.commands.SetGroupCommand;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class NamelessPlugin extends Plugin {

	private static NamelessPlugin instance;

	public static URL baseApiURL;
	public static boolean https;

	public static final String permission = "namelessmc";
	public static final String permissionAdmin = "namelessmc.admin";

	@Override
	public void onEnable() {
		instance = this;

		try {
			Config.initialize();
		} catch (IOException e) {
			NamelessChat.log(Level.SEVERE, "Unable to load config.");
			e.printStackTrace();
			return;
		}
		
		Configuration config = Config.MAIN.getConfig();
		String url = config.getString("api-url");
		if (url.equals("")){
			NamelessChat.log(Level.SEVERE, "&4No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			return; //Prevent registering of commands, listeners, etc.
		} else {
			try {
				baseApiURL = new URL(url);
			} catch (MalformedURLException e) {
				//There is an exception, so the connection was not successful.
				NamelessChat.log(Level.SEVERE, "&4Invalid API Url/Key. Nothing will work until you set the correct url.");
				NamelessChat.log(Level.SEVERE, "Error: " + e.getMessage());
				return; //Prevent registering of commands, listeners, etc.
			}
			
			Exception exception = NamelessAPI.checkWebAPIConnection(baseApiURL);
			if (exception != null) {
				//There is an exception, so the connection was not successful.
				NamelessChat.log(Level.SEVERE, "&4Invalid API Url/Key. Nothing will work until you set the correct url.");
				NamelessChat.log(Level.SEVERE, "Error: " + exception.getMessage());
				return; //Prevent registering of commands, listeners, etc.
			}
		}
		
		//Connection is successful, move on with registering listeners and commands.
		
		registerCommands();
		
		getProxy().getPluginManager().registerListener(this, new PlayerEventListener());
	}
	
	private void registerCommands() {
		Configuration commandsConfig = Config.COMMANDS.getConfig();
		
		if (commandsConfig.getBoolean("Commands.Alone.Use") && commandsConfig.getBoolean("Commands.SubCommand.Use")) {
			NamelessChat.log(Level.WARNING, "&4Both individual and subcommands are set to true. Please choose either one. Commands will not work until fixed.");
			return;
		}
		
		if (commandsConfig.getBoolean("Commands.Alone.Use")) {
			if (commandsConfig.getBoolean("enable-registration"))
				getProxy().getPluginManager().registerCommand(this, new RegisterCommand(commandsConfig.getString("Commands.Alone.Register")));

			getProxy().getPluginManager().registerCommand(this, new GetUserCommand(commandsConfig.getString("Commands.Alone.GetUser")));
			
			getProxy().getPluginManager().registerCommand(this, new GetNotificationsCommand(commandsConfig.getString("Commands.Alone.GetNotifications")));
			
			getProxy().getPluginManager().registerCommand(this, new SetGroupCommand(commandsConfig.getString("Commands.Alone.SetGroup")));
			
			if (commandsConfig.getBoolean("enable-reports"))
				getProxy().getPluginManager().registerCommand(this, new ReportCommand(commandsConfig.getString("Commands.Alone.Report")));
				
		} else {
			getProxy().getPluginManager().registerCommand(this, new CommandWithArgs(commandsConfig.getString("Commands.SubCommand.Main")));
		}
	}
	
	public static NamelessPlugin getInstance() {
		return instance;
	}

}