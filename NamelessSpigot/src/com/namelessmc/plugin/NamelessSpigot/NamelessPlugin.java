package com.namelessmc.plugin.NamelessSpigot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.plugin.NamelessSpigot.commands.CommandWithArgs;
import com.namelessmc.plugin.NamelessSpigot.commands.GetNotificationsCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.GetUserCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.RegisterCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.ReportCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.SetGroupCommand;
import com.namelessmc.plugin.NamelessSpigot.commands.nameless.NLCommand;
import com.namelessmc.plugin.NamelessSpigot.hooks.MVdWPlaceholderUtil;
import com.namelessmc.plugin.NamelessSpigot.hooks.PAPIPlaceholderUtil;
import com.namelessmc.plugin.NamelessSpigot.player.PlayerEventListener;

import net.milkbowl.vault.permission.Permission;

public class NamelessPlugin extends JavaPlugin {

	/*
	 * Instance
	 */
	private static NamelessPlugin instance;

	/*
	 * API URL
	 */
	public static URL baseApiURL;
	public static boolean https;

	/*
	 * NamelessMC permission string.
	 */

	public static final String PERMISSION = "namelessmc";
	public static final String PERMISSION_ADMIN = "namelessmc.admin";
	/*
	 * Vault
	 */
	boolean useVault = false;
	private Permission permissions = null;

	/*
	 * Groups Support
	 */
	boolean useGroups = false;

	/*
	 * Spigot or Bukkit?
	 */
	private boolean spigot = true;

	/*
	 * Bukkit command maps
	 */
	private Field bukkitCommandMap;
	private CommandMap commandMap;

	@Override
	public void onLoad() {
		NamelessPlugin.instance = this;
	}

	/*
	 * OnEnable method
	 */
	@Override
	public void onEnable() {
		// Check Sofware (Spigot or Bukkit)
		checkSoftware();

		if (isSpigot()) {
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
			
			initHooks();
			
			// Connection is successful, move on with registering listeners and commands.
			registerCommands();
			getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
			
			// Start saving data files every 15 minutes
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveConfig(), 5*60*20, 5*60*20);
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

	// Currently disabled.
	/*public void checkForUpdate() {
		if (getAPI().getConfigManager().getConfig().getBoolean("update-checker")) {
			UpdateChecker updateChecker = new UpdateChecker(this);
			if (updateChecker.updateNeeded()) {
				for (String msg : updateChecker.getConsoleUpdateMessage()) {
					NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, msg);
				}
			} else {
				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aFound no new updates!");
			}
		} else {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
					"&CIt is recommended to enable update checker.");
		}
	}*/

	private void registerCommands() {

		getServer().getPluginCommand("nameless").setExecutor(new NLCommand());
		
		YamlConfiguration commandsConfig = Config.COMMANDS.getConfig();

		try {
			bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
			commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}

		String namelessMC = this.getName();

		boolean subcommands = Config.COMMANDS.getConfig().getBoolean("subcommands.enabled", true);
		boolean individual = Config.COMMANDS.getConfig().getBoolean("individual.enabled", true);

		if (individual) {
			if (commandsConfig.getBoolean("enable-registration"))
				commandMap.register(namelessMC, 
						new RegisterCommand(commandsConfig.getString("commands.individual.register")));

			commandMap.register(namelessMC, 
					new GetUserCommand(commandsConfig.getString("commands.individual.user-info")));

			commandMap.register(namelessMC, 
					new GetNotificationsCommand(commandsConfig.getString("commands.individual.notifications")));

			commandMap.register(namelessMC, 
					new SetGroupCommand(commandsConfig.getString("commands.individual.setgroup")));

			if (commandsConfig.getBoolean("enable-reports"))
				commandMap.register(namelessMC, 
						new ReportCommand(commandsConfig.getString("commands.individual.report")));

		}

		if (subcommands) {
			commandMap.register(namelessMC, 
					new CommandWithArgs(commandsConfig.getString("commands.subcommands.main")));
		}
	}

	public void checkSoftware() {

		// DISABLED BUKKIT FOR NOW.
		try {
			Class.forName("org.spigotmc.Metrics");
		} catch (Exception e) {
			spigot = false;
			Chat.log(Level.SEVERE, "&4The plugin only works with spigot not bukkit!");
			Chat.log(Level.SEVERE, "&4To solve this issue get spigot, disabling.");
			getServer().getPluginManager().disablePlugin(this);
		}

	}

	/*
	 * Check if Vault is Activated
	 */
	public void detectVault() {
		if (getServer().getPluginManager().getPlugin("Vault") != null) {
			// Enable Vault integration and setup Permissions.
			useVault = true;
			initPermissions();
			// Check if the permissions plugin has groups.
			if (permissions.hasGroupSupport()) {
				useGroups = true;
			} else {
				Chat.log(Level.WARNING,
						"&4Permissions plugin does NOT support groups! Disabling NamelessMC Vault integration.");
				useGroups = false;
			}
		} else {
			Chat.log(Level.WARNING, "&4Couldn't detect Vault, disabling NamelessMC Vault integration.");
		}
	}

	/*
	 * Initialize hooks
	 */
	private void initHooks() {
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			MVdWPlaceholderUtil placeholders = new MVdWPlaceholderUtil(this);
			placeholders.hook();
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			PAPIPlaceholderUtil placeholders = new PAPIPlaceholderUtil(this);
			placeholders.hook();
		}
	}

	/*
	 * Initialise Vault permissions integration for group sync
	 */
	private boolean initPermissions() {

		if (useVault) {
			RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager()
					.getRegistration(Permission.class);
			permissions = rsp.getProvider();
		}

		return permissions != null;
	}

	/*
	 * Get / Has / Set
	 */

	// Gets the instance
	public static NamelessPlugin getInstance() {
		return instance;
	}

	// Check if Spigot
	public boolean isSpigot() {
		return spigot;
	}

	// Check if Bukkit
	public boolean isBukkit() {
		return !spigot;
	}

	// Set Spigot (true or false)
	public void setSpigot(boolean value) {
		spigot = value;
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