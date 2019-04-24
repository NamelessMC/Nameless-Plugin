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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.plugin.NamelessSpigot.commands.Command;
import com.namelessmc.plugin.NamelessSpigot.commands.PluginCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.SubCommands;
import com.namelessmc.plugin.NamelessSpigot.event.PlayerLogin;
import com.namelessmc.plugin.NamelessSpigot.event.PlayerQuit;
import com.namelessmc.plugin.NamelessSpigot.hooks.MVdWPapiHook;
import com.namelessmc.plugin.NamelessSpigot.hooks.PapiHook;
import com.namelessmc.plugin.NamelessSpigot.hooks.PapiParser;
import com.namelessmc.plugin.NamelessSpigot.hooks.PapiParserDisabled;
import com.namelessmc.plugin.NamelessSpigot.hooks.PapiParserEnabled;
import com.namelessmc.plugin.NamelessSpigot.hooks.PlaceholderCacher;

import net.milkbowl.vault.economy.Economy;

public class NamelessPlugin extends JavaPlugin {

	private static NamelessPlugin instance;
	
	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();
	
	public static net.milkbowl.vault.permission.Permission permissions;
	public static Economy economy;
	
	public NamelessAPI api;
	
	public PapiParser papiParser;
	
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
			
		if (!initApi()) return;
			
		initHooks();
			
		// Connection is successful, move on with registering listeners and commands.
		registerCommands();
		getServer().getPluginManager().registerEvents(new PlayerLogin(), this);
		getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
		
		// Start saving data files every 15 minutes
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveConfig(), 5*60*20, 5*60*20);

		int uploadPeriod = Config.MAIN.getConfig().getInt("server-data-upload-rate", 10) * 20;
		if (uploadPeriod > 0) {
			new ServerDataSender().runTaskTimer(this, uploadPeriod, uploadPeriod);
		}
		
		// For reloads
		for (Player player : Bukkit.getOnlinePlayers()) {
			LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());
		}
		
		// In the start function there is a check if the feature is actually enabled
		new WhitelistRegistered().start();
	}
	
	@Override
	public void onDisable() {
		// Save all configuration files that require saving
		for (Config config : Config.values()) {
			if (config.autoSave()) config.save();
		}
	}
	
	private boolean initApi() {
		FileConfiguration config = Config.MAIN.getConfig();
		String url = config.getString("api-url");
		if (url.equals("")) {
			log(Level.SEVERE, "No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			return false; // Prevent registering of commands, listeners, etc.
		} else {
			URL apiUrl;
			try {
				apiUrl = new URL(url);
			} catch (MalformedURLException e) {
				// There is an exception, so the connection was not successful.
				log(Level.SEVERE, "Syntax error in API URL. Nothing will work until you set the correct url.");
				log(Level.SEVERE, "Error: " + e.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}
			
			boolean debug = config.getBoolean("api-debug-mode", false);
			
			api = new NamelessAPI(apiUrl, debug);

			Exception exception = api.checkWebAPIConnection();
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
		getServer().getPluginCommand("namelessplugin").setExecutor(new PluginCommand());
		
		try {
			Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			CommandMap map = (CommandMap) field.get(Bukkit.getServer());
			
			String name = this.getName(); //Get name of plugin from config.yml just in case we ever change it

			boolean subcommands = Config.COMMANDS.getConfig().getBoolean("subcommands.enabled", true);
			boolean individual = Config.COMMANDS.getConfig().getBoolean("individual.enabled", true);

			if (individual) {				
				for (Command command : Command.COMMANDS) {
					if (command.getName().equals("disabled"))
						continue;

					map.register(name, command);
				}
			}

			if (subcommands) {
				map.register(name, new SubCommands());
			}
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	private void initHooks() {
		boolean placeholderPluginInstalled = false;
		
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			MVdWPapiHook placeholders = new MVdWPapiHook();
			placeholders.hook();
			placeholderPluginInstalled = true;
		}
		
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			PapiHook placeholders = new PapiHook();
			placeholders.hook();
			placeholderPluginInstalled = true;
			
			papiParser = new PapiParserEnabled();
		} else {
			papiParser = new PapiParserDisabled();
		}
		
		if (placeholderPluginInstalled && Config.MAIN.getConfig().getBoolean("enable-placeholders", false)) {
			Bukkit.getScheduler().runTaskAsynchronously(this, new PlaceholderCacher());
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
				for (Config config : Config.values()) {
					if (config.autoSave())
						config.save();
				}
			});
		}

	}

}