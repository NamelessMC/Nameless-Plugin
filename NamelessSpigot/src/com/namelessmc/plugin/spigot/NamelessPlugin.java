package com.namelessmc.plugin.spigot;

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
import com.namelessmc.plugin.spigot.commands.Command;
import com.namelessmc.plugin.spigot.commands.PluginCommand;
import com.namelessmc.plugin.spigot.commands.SubCommands;
import com.namelessmc.plugin.spigot.event.PlayerLogin;
import com.namelessmc.plugin.spigot.event.PlayerQuit;
import com.namelessmc.plugin.spigot.hooks.PapiHook;
import com.namelessmc.plugin.spigot.hooks.PapiParser;
import com.namelessmc.plugin.spigot.hooks.PapiParserDisabled;
import com.namelessmc.plugin.spigot.hooks.PapiParserEnabled;
import com.namelessmc.plugin.spigot.hooks.PlaceholderCacher;

import net.milkbowl.vault.economy.Economy;

public class NamelessPlugin extends JavaPlugin {

	private static NamelessPlugin instance;

	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	public static net.milkbowl.vault.permission.Permission permissions;
	public static Economy economy;

	public NamelessAPI api;

	public PapiParser papiParser;

	@Override
	public void onLoad() {
		instance = this;

		try {
			Config.initialize();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onEnable() {
		if (!this.initApi()) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			final RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider == null) {
				log(Level.WARNING, "No vault compatible permissions plugin was found. Rank sync will not work.");
			} else {
				permissions = permissionProvider.getProvider();

				if (permissions == null) {
					log(Level.WARNING, "No vault compatible permissions plugin was found. Rank sync will not work.");
				}
			}

			final RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
			if (economyProvider == null) {
				log(Level.WARNING, "No economy plugin was found.");
			} else {
				economy = economyProvider.getProvider();

				if (economy == null) {
					log(Level.WARNING, "No economy plugin was found.");
				}
			}
		} else {
			log(Level.WARNING, "Vault was not found. Rank sync will not work.");
		}
		
		this.initHooks();

		// Connection is successful, move on with registering listeners and commands.
		this.registerCommands();
		this.getServer().getPluginManager().registerEvents(new PlayerLogin(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerQuit(), this);

		// Start saving data files every 15 minutes
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveConfig(), 5*60*20, 5*60*20);

		final int uploadPeriod = Config.MAIN.getConfig().getInt("server-data-upload-rate", 10) * 20;
		if (uploadPeriod > 0) {
			new ServerDataSender().runTaskTimer(this, uploadPeriod, uploadPeriod);
		}

		// For reloads
		for (final Player player : Bukkit.getOnlinePlayers()) {
			LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());
		}

		new WhitelistRegistered(); // In the constructor there is a check if the feature is actually enabled
	}

	@Override
	public void onDisable() {
		// Save all configuration files that require saving
		for (final Config config : Config.values()) {
			if (config.autoSave()) {
				config.save();
			}
		}
	}

	private boolean initApi() {
		final FileConfiguration config = Config.MAIN.getConfig();
		final String url = config.getString("api-url");
		if (url.equals("")) {
			log(Level.SEVERE, "No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			log(Level.SEVERE, "After fixing the issue, restart the server.");
			return false; // Prevent registering of commands, listeners, etc.
		} else {
			URL apiUrl;
			try {
				apiUrl = new URL(url);
			} catch (final MalformedURLException e) {
				// There is an exception, so the connection was not successful.
				log(Level.SEVERE, "Syntax error in API URL. Nothing will work until you set the correct url.");
				log(Level.SEVERE, "After fixing the issue, restart the server.");
				log(Level.SEVERE, "Error: " + e.getMessage());
				return false; // Prevent registering of commands, listeners, etc.
			}

			final boolean debug = config.getBoolean("api-debug-mode", false);

			this.api = new NamelessAPI(apiUrl, debug);

			if (config.contains("user-agent")) {
				this.api.setUserAgent(config.getString("user-agent"));
			}

			final Exception exception = this.api.checkWebAPIConnection();
			if (exception != null) {
				// There is an exception, so the connection was unsuccessful.
				log(Level.SEVERE, "Invalid API URL/key. Nothing will work until you set the correct url.");
				log(Level.SEVERE, "After fixing the issue, restart the server.");
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
		this.getServer().getPluginCommand("namelessplugin").setExecutor(new PluginCommand());

		try {
			final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			final CommandMap map = (CommandMap) field.get(Bukkit.getServer());

			final String name = this.getName(); //Get name of plugin from config.yml just in case we ever change it

			final boolean subcommands = Config.COMMANDS.getConfig().getBoolean("subcommands.enabled", true);
			final boolean individual = Config.COMMANDS.getConfig().getBoolean("individual.enabled", true);

			if (individual) {
				for (final Command command : Command.COMMANDS) {
					if (command.getName().equals("disabled")) {
						continue;
					}

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

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			final PapiHook placeholders = new PapiHook();
			placeholders.register();
			placeholderPluginInstalled = true;

			this.papiParser = new PapiParserEnabled();
		} else {
			this.papiParser = new PapiParserDisabled();
		}

		if (placeholderPluginInstalled && Config.MAIN.getConfig().getBoolean("enable-placeholders", false)) {
			Bukkit.getScheduler().runTaskAsynchronously(this, new PlaceholderCacher());
		}
	}

	public static NamelessPlugin getInstance() {
		return instance;
	}

	public static void log(final Level level, final String message) {
		NamelessPlugin.getInstance().getLogger().log(level, message);
	}

	public static class SaveConfig implements Runnable {

		@Override
		public void run() {
			final NamelessPlugin plugin = NamelessPlugin.getInstance();
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				for (final Config config : Config.values()) {
					if (config.autoSave()) {
						config.save();
					}
				}
			});
		}

	}

}