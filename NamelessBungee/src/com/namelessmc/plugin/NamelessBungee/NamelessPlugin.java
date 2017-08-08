package com.namelessmc.plugin.NamelessBungee;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.commands.CommandWithArgs;
import com.namelessmc.plugin.NamelessBungee.commands.GetNotificationsCommand;
import com.namelessmc.plugin.NamelessBungee.commands.GetUserCommand;
import com.namelessmc.plugin.NamelessBungee.commands.NamelessCommand;
import com.namelessmc.plugin.NamelessBungee.commands.RegisterCommand;
import com.namelessmc.plugin.NamelessBungee.commands.ReportCommand;
import com.namelessmc.plugin.NamelessBungee.commands.SetGroupCommand;
import com.namelessmc.plugin.NamelessBungee.player.PlayerEventListener;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

public class NamelessPlugin extends Plugin {

	public static URL baseApiURL;
	public static boolean https;

	private static NamelessPlugin instance;

	@Override
	public void onLoad() {
		instance = this;
	}
	
	@Override
	public void onEnable() {
		try {
			Config.initialize();
		} catch (IOException e) {
			Chat.log(Level.SEVERE, "Unable to load config.");
			e.printStackTrace();
			return;
		}

		if (!checkConnection()) return;

		// Connection is successful, move on with registering listeners and commands.
		
		registerCommands();

		getProxy().getPluginManager().registerListener(this, new PlayerEventListener());

		// Start saving data files every 15 minutes
		getProxy().getScheduler().schedule(this, new SaveConfig(), 15L, 15L, TimeUnit.MINUTES);
		
		// Start group synchronization task
		Configuration config = Config.MAIN.getConfig();
		if (!(config.getInt("group-synchronization.sync-interval") <= 0)) {
			long interval = config.getInt("group-synchronization.sync-interval");
			getProxy().getScheduler().schedule(this, () ->{
				for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
					syncGroup(player);
				}
			}, interval, interval, TimeUnit.SECONDS);
		}
	}

	@Override
	public void onDisable() {
		// Save all configuration files that require saving
		try {
			for (Config config : Config.values()) {
				if (config.autoSave()) config.saveConfig();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkConnection() {
		Configuration config = Config.MAIN.getConfig();
		String url = config.getString("api-url");
		if (url.equals("")) {
			Chat.log(Level.SEVERE, "No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			return false; // Prevent registering of commands, listeners, etc.
		} else {
			try {
				baseApiURL = new URL(url);
			} catch (MalformedURLException e) {
				// There is an exception, so the connection was not successful.
				Chat.log(Level.SEVERE, "Invalid API Url/Key. Nothing will work until you set the correct url.");
				Chat.log(Level.SEVERE, "Error: " + e.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}

			Exception exception = NamelessAPI.checkWebAPIConnection(baseApiURL);
			if (exception != null) {
				// There is an exception, so the connection was unsuccessful.
				Chat.log(Level.SEVERE, "Invalid API Url/Key. Nothing will work until you set the correct url.");
				Chat.log(Level.SEVERE, "Error: " + exception.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}
		}
		return true;
	}

	private void registerCommands() {
		Configuration commandsConfig = Config.COMMANDS.getConfig();
		PluginManager pm = getProxy().getPluginManager();
		
		pm.registerCommand(this, new NamelessCommand());

		boolean subcommands = Config.COMMANDS.getConfig().getBoolean("subcommands.enabled", true);
		boolean individual = Config.COMMANDS.getConfig().getBoolean("individual.enabled", true);

		if (individual) {
			if (commandsConfig.getBoolean("enable-registration")) {
				pm.registerCommand(this, new RegisterCommand(commandsConfig.getString("individual.register")));
			}

			pm.registerCommand(this, new GetUserCommand(commandsConfig.getString("individual.user-info")));

			pm.registerCommand(this, new GetNotificationsCommand(commandsConfig.getString("individual.notifications")));

			pm.registerCommand(this, new SetGroupCommand(commandsConfig.getString("individual.setgroup")));

			if (commandsConfig.getBoolean("enable-reports")) {
				pm.registerCommand(this, new ReportCommand(commandsConfig.getString("individual.report")));
			}
		}
		
		if (subcommands) {
			pm.registerCommand(this, new CommandWithArgs(commandsConfig.getString("subcommands.main")));
		}
	}

	public static NamelessPlugin getInstance() {
		return instance;
	}
	
	public static void syncGroup(ProxiedPlayer player) {
		Configuration permissionConfig = Config.GROUP_SYNC_PERMISSIONS.getConfig();
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			for (String groupID : permissionConfig.getSection("permissions").getKeys()) {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				if(!namelessPlayer.exists()) return;
				if (String.valueOf(namelessPlayer.getGroupID()).equals(groupID))
					return;
					
				if (player.hasPermission(permissionConfig.getString("permissions." + groupID))) {
					int previousGroup = namelessPlayer.getGroupID();

					try {
						namelessPlayer.setGroup(Integer.parseInt(groupID));
						Chat.log(Level.INFO, "Successfully changed " + player.getName() + "'s group from "+ previousGroup + " to " + groupID + ".");								
					} catch (NumberFormatException e) {
						Chat.log(Level.WARNING, String.format("The provided group id \"%s\" is not a number.", groupID));
					} catch (NamelessException e) {
						Chat.log(Level.WARNING, "Error changing "+ player.getName() + "'s group.");
						e.printStackTrace();
					}
				}
			}
		});
		
	}

	public static class SaveConfig implements Runnable {

		@Override
		public void run() {
			NamelessPlugin plugin = NamelessPlugin.getInstance();
			plugin.getProxy().getScheduler().runAsync(plugin, () -> {
				try {
					for (Config config : Config.values()) {
						if (config.autoSave()) config.saveConfig();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

	}

}