package com.namelessmc.plugin.spigot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.AbstractYamlFile;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandlerProvider;
import com.namelessmc.plugin.common.NamelessApiProvider;
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

public class NamelessPlugin extends JavaPlugin implements NamelessApiProvider, LanguageHandlerProvider<CommandSender> {

	private static final Function<Path, AbstractYamlFile> LANGUAGE_FILE_LOADER = (file) -> {
		return new YamlFileImpl(YamlConfiguration.loadConfiguration(file.toFile()));
	};

	private static NamelessPlugin instance;

	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	public static net.milkbowl.vault.permission.Permission permissions;
	public static Economy economy;

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

	private LanguageHandler<CommandSender> language;

	@Override
	public void onEnable() {
		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			final RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider == null) {
				log(Level.WARNING, "No vault compatible permissions plugin was found. Group sync will not work.");
			} else {
				permissions = permissionProvider.getProvider();

				if (permissions == null) {
					log(Level.WARNING, "No vault compatible permissions plugin was found. Group sync will not work.");
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
			log(Level.WARNING, "Vault was not found. Group sync will not work.");
		}

		this.language = new LanguageHandler<>(CommandSender::sendMessage);

		try {
			this.getLanguageHandler().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (!this.getLanguageHandler().setActiveLanguage(Config.MAIN.getConfig().getString("language", LanguageHandler.DEFAULT_LANGUAGE), LANGUAGE_FILE_LOADER)) {
			this.getLogger().severe("LANGUAGE FILE FAILED TO LOAD");
			this.getLogger().severe("THIS IS BAD NEWS, THE PLUGIN WILL BREAK");
			this.getLogger().severe("FIX IMMEDIATELY");
			this.getLogger().severe("In config.yml, set 'language' to '" + LanguageHandler.DEFAULT_LANGUAGE + "' or any other supported language.");
			return;
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

		getServer().getScheduler().runTaskAsynchronously(this, this::checkUuids);
	}

	private void checkUuids() {
		@SuppressWarnings("deprecation")
		final OfflinePlayer notch = Bukkit.getOfflinePlayer("Notch");
		if (notch.getUniqueId().toString().equals("069a79f4-44e9-4726-a5be-fca90e38aaf5")) {
			getLogger().info("UUIDs are working properly.");
		} else {
			getLogger().severe("*** IMPORTANT ***");
			getLogger().severe("Your server does not use Mojang UUIDs!");
			getLogger().severe("This plugin won't work for cracked servers. If you do not intend to run a cracked server and you use BungeeCord, make sure `bungeecord: true` is set in spigot.yml and ip forwarding is enabled in the BungeeCord config file.");
		}
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

	public void reload() {
		NamelessPlugin.instance.reloadConfig();
		cachedApi = null;
		for (final Config config : Config.values()) {
			config.reload();
		}
		try {
			this.getLanguageHandler().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		this.getLanguageHandler().setActiveLanguage(Config.MAIN.getConfig().getString("language", LanguageHandler.DEFAULT_LANGUAGE), LANGUAGE_FILE_LOADER);
	}

	private static final String USER_AGENT = "Nameless-Plugin";
	private static NamelessAPI cachedApi = null;

	@Override
	public NamelessAPI getNamelessApi() throws NamelessException {
		if (cachedApi != null) {
			return cachedApi;
		}

		final FileConfiguration config = NamelessPlugin.getInstance().getConfig();
		final boolean debug = config.getBoolean("api-debug-mode", false);
		final URL apiUrl;
		try {
			apiUrl = new URL(config.getString("api-url"));
		} catch (final MalformedURLException e) {
			throw new NamelessException("Malformed URL", e);
		}

		cachedApi = new NamelessAPI(apiUrl, USER_AGENT, debug);
		return cachedApi;
	}

	@Override
	public LanguageHandler<CommandSender> getLanguageHandler() {
		return this.language;
	}

	private void registerCommands() {
		this.getServer().getPluginCommand("namelessplugin").setExecutor(new PluginCommand());

		try {
			final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			final CommandMap map = (CommandMap) field.get(Bukkit.getServer());

			final String name = this.getName();

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