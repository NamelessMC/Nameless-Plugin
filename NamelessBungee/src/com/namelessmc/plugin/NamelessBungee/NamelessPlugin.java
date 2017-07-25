package com.namelessmc.plugin.NamelessBungee;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.plugin.NamelessBungee.commands.CommandWithArgs;
import com.namelessmc.plugin.NamelessBungee.commands.GetNotificationsCommand;
import com.namelessmc.plugin.NamelessBungee.commands.GetUserCommand;
import com.namelessmc.plugin.NamelessBungee.commands.RegisterCommand;
import com.namelessmc.plugin.NamelessBungee.commands.ReportCommand;
import com.namelessmc.plugin.NamelessBungee.commands.SetGroupCommand;
import com.namelessmc.plugin.NamelessBungee.player.PlayerEventListener;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class NamelessPlugin extends Plugin {

	public static final String PERMISSION = "namelessmc";
	public static final String PERMISSION_ADMIN = "namelessmc.admin";

	public static URL baseApiURL;
	public static boolean https;

	private static NamelessPlugin instance;

	@Override
	public void onEnable() {
		instance = this;

		try {
			Config.initialize();
		} catch (IOException e) {
			Chat.log(Level.SEVERE, "&4Unable to load config.");
			e.printStackTrace();
			return;
		}

		if (!checkConnection()) {
			return;
		}

		// Connection is successful, move on with registering listeners and commands.
		
		registerCommands();

		getProxy().getPluginManager().registerListener(this, new PlayerEventListener());

		// Start saving data files every 15 minutes
		getProxy().getScheduler().schedule(this, new SaveConfig(), 15L, 15L, TimeUnit.MINUTES);
	}

	@Override
	public void onDisable() {
		// Save all configuration files
		try {
			for (Config config : Config.values()) {
				config.saveConfig();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkConnection() {
		Configuration config = Config.MAIN.getConfig();
		String url = config.getString("api-url");
		if (url.equals("")) {
			Chat.log(Level.SEVERE, "&4No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			return false; // Prevent registering of commands, listeners, etc.
		} else {
			try {
				baseApiURL = new URL(url);
			} catch (MalformedURLException e) {
				// There is an exception, so the connection was not successful.
				Chat.log(Level.SEVERE, "&4Invalid API Url/Key. Nothing will work until you set the correct url.");
				Chat.log(Level.SEVERE, "Error: " + e.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}

			Exception exception = NamelessAPI.checkWebAPIConnection(baseApiURL);
			if (exception != null) {
				// There is an exception, so the connection was unsuccessful.
				Chat.log(Level.SEVERE, "&4Invalid API Url/Key. Nothing will work until you set the correct url.");
				Chat.log(Level.SEVERE, "Error: " + exception.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}
		}
		return true;
	}

	private void registerCommands() {
		Configuration commandsConfig = Config.COMMANDS.getConfig();

		boolean subcommands = Config.COMMANDS.getConfig().getBoolean("subcommands.enabled", true);
		boolean individual = Config.COMMANDS.getConfig().getBoolean("individual.enabled", true);

		if (individual) {
			if (commandsConfig.getBoolean("enable-registration"))
				getProxy().getPluginManager().registerCommand(this,
						new RegisterCommand(commandsConfig.getString("Commands.Alone.Register")));

			getProxy().getPluginManager().registerCommand(this,
					new GetUserCommand(commandsConfig.getString("Commands.Alone.GetUser")));

			getProxy().getPluginManager().registerCommand(this,
					new GetNotificationsCommand(commandsConfig.getString("Commands.Alone.GetNotifications")));

			getProxy().getPluginManager().registerCommand(this,
					new SetGroupCommand(commandsConfig.getString("Commands.Alone.SetGroup")));

			if (commandsConfig.getBoolean("enable-reports"))
				getProxy().getPluginManager().registerCommand(this,
						new ReportCommand(commandsConfig.getString("Commands.Alone.Report")));

		}
		
		if (subcommands) {
			getProxy().getPluginManager().registerCommand(this,
					new CommandWithArgs(commandsConfig.getString("Commands.SubCommand.Main")));
		}
	}

	public static NamelessPlugin getInstance() {
		return instance;
	}

	public static class SaveConfig implements Runnable {

		@Override
		public void run() {
			NamelessPlugin plugin = NamelessPlugin.getInstance();
			plugin.getProxy().getScheduler().runAsync(plugin, () -> {
				try {
					for (Config config2 : Config.values()) {
						config2.saveConfig();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

	}

}