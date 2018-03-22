package com.namelessmc.plugin.NamelessSpigot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.plugin.NamelessSpigot.commands.CommandWithArgs;
import com.namelessmc.plugin.NamelessSpigot.commands.GetNotificationsCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.UserInfoCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.NamelessCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.RegisterCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.ReportCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.SetGroupCommand;
import com.namelessmc.plugin.NamelessSpigot.event.PlayerLogin;
import com.namelessmc.plugin.NamelessSpigot.event.PlayerQuit;
import com.namelessmc.plugin.NamelessSpigot.hooks.MVdWPlaceholderUtil;
import com.namelessmc.plugin.NamelessSpigot.hooks.PAPIPlaceholderUtil;

import net.milkbowl.vault.economy.Economy;

public class NamelessPlugin extends JavaPlugin {

	private static NamelessPlugin instance;

	public static URL baseApiURL;

	boolean useGroups = false;
	
	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();
	
	public static net.milkbowl.vault.permission.Permission permissions;
	public static Economy economy;
	
	@Override
	public void onEnable() {
		instance = this;
		
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			log(Level.SEVERE, "This plugin requires Vault. Please install Vault and restart.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider == null) {
			log(Level.SEVERE, "You do not have a vault-compatible permissions plugin. Please install one and restart.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		permissions = permissionProvider.getProvider();
		
		if (permissions == null) {
			log(Level.SEVERE, "You do not have a vault-compatible permissions plugin. Please install one and restart.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) economy = economyProvider.getProvider();

		try {
			Config.initialize();
		} catch (IOException e) {
			log(Level.SEVERE, "Unable to load config.");
			e.printStackTrace();
			return;
		}
			
		if (!checkConnection()) return;
			
		initHooks();
			
		// Connection is successful, move on with registering listeners and commands.
		registerCommands();
		getServer().getPluginManager().registerEvents(new PlayerLogin(), this);
		getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
			
		// Start saving data files every 15 minutes
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveConfig(), 5*60*20, 5*60*20);

		int uploadPeriod = Config.MAIN.getConfig().getInt("server-data-upload-rate", 10) * 20;
		new ServerDataSender().runTaskTimer(this, uploadPeriod, uploadPeriod);
		
		// For reloads
		for (Player player : Bukkit.getOnlinePlayers()) {
			LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());
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
		YamlConfiguration config = Config.MAIN.getConfig();
		String url = config.getString("api-url");
		if (url.equals("")) {
			log(Level.SEVERE, "No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			return false; // Prevent registering of commands, listeners, etc.
		} else {
			try {
				baseApiURL = new URL(url);
			} catch (MalformedURLException e) {
				// There is an exception, so the connection was not successful.
				log(Level.SEVERE, "Syntax error in API URL. Nothing will work until you set the correct url.");
				log(Level.SEVERE, "Error: " + e.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}

			Exception exception = NamelessAPI.checkWebAPIConnection(baseApiURL);
			if (exception != null) {
				// There is an exception, so the connection was unsuccessful.
				log(Level.SEVERE, "Invalid API URL/key. Nothing will work until you set the correct url.");
				log(Level.SEVERE, "Error: " + exception.getMessage());
				log(Level.SEVERE, "");
				log(Level.WARNING, "");
				exception.printStackTrace();
				return false; // Prevent registering of commands, listeners, etc.
			}
		}
		return true;
	}

	private void registerCommands() {
		getServer().getPluginCommand("nameless").setExecutor(new NamelessCommand());
		
		YamlConfiguration commandsConfig = Config.COMMANDS.getConfig();
		
		try {
			Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			CommandMap map = (CommandMap) field.get(Bukkit.getServer());
			
			String name = this.getName(); //Get name of plugin from config.yml just in case we ever change it

			boolean subcommands = Config.COMMANDS.getConfig().getBoolean("subcommands.enabled", true);
			boolean individual = Config.COMMANDS.getConfig().getBoolean("individual.enabled", true);

			if (individual) {
				if (commandsConfig.getBoolean("enable-registration")) {
					map.register(name, new RegisterCommand(commandsConfig.getString("individual.register")));
				}

				map.register(name, new UserInfoCommand(commandsConfig.getString("individual.user-info")));

				map.register(name, new GetNotificationsCommand(commandsConfig.getString("individual.get-notifications")));

				map.register(name, new SetGroupCommand(commandsConfig.getString("individual.set-group")));

				if (commandsConfig.getBoolean("enable-reports")) {
					map.register(name, new ReportCommand(commandsConfig.getString("individual.report")));
				}
			}

			if (subcommands) {
				map.register(name, new CommandWithArgs(commandsConfig.getString("subcommands.main")));
			}
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	private void initHooks() {
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			MVdWPlaceholderUtil placeholders = new MVdWPlaceholderUtil();
			placeholders.hook();
		}
		
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			PAPIPlaceholderUtil placeholders = new PAPIPlaceholderUtil();
			placeholders.hook();
		}
	}

	public static NamelessPlugin getInstance() {
		return instance;
	}
	
	public static void log(Level level, String message) {
		NamelessPlugin.getInstance().getLogger().log(level, message);
	}

	public static class SaveConfig implements Runnable {

		@Override
		public void run() {
			NamelessPlugin plugin = NamelessPlugin.getInstance();
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					for (Config config : Config.values()) {
						if (config.autoSave())
							config.saveConfig();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

	}

}