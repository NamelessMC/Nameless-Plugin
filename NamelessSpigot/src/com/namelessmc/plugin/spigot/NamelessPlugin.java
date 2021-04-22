package com.namelessmc.plugin.spigot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandlerProvider;
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

public class NamelessPlugin extends JavaPlugin implements LanguageHandlerProvider<CommandSender> {

	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	private static NamelessPlugin instance;

	private ApiProviderImpl apiProvider;
	private LanguageHandler<CommandSender> language;
	private net.milkbowl.vault.permission.Permission permissions;
	private Economy economy;
	private PapiParser papiParser;

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		try {
			Config.initialize();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			final RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider == null) {
				log(Level.WARNING, "No vault compatible permissions plugin was found. Group sync will not work.");
			} else {
				this.permissions = permissionProvider.getProvider();

				if (this.permissions == null) {
					log(Level.WARNING, "No vault compatible permissions plugin was found. Group sync will not work.");
				}
			}

			final RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
			if (economyProvider == null) {
				log(Level.WARNING, "No economy plugin was found.");
			} else {
				this.economy = economyProvider.getProvider();

				if (this.economy == null) {
					log(Level.WARNING, "No economy plugin was found.");
				}
			}
		} else {
			log(Level.WARNING, "Vault was not found. Group sync will not work.");
		}

		this.language = new LanguageHandler<>(getDataFolder().toPath().resolve("languages"), CommandSender::sendMessage);

		reload();

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
		this.apiProvider.loadConfiguration(getConfig());
		for (final Config config : Config.values()) {
			config.reload();
		}
		try {
			this.getLanguageHandler().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		if (!this.getLanguageHandler().setActiveLanguage(Config.MAIN.getConfig().getString("language", LanguageHandler.DEFAULT_LANGUAGE), YamlFileImpl::new)) {
			this.getLogger().severe("LANGUAGE FILE FAILED TO LOAD");
			this.getLogger().severe("THIS IS BAD NEWS, THE PLUGIN WILL BREAK");
			this.getLogger().severe("FIX IMMEDIATELY");
			this.getLogger().severe("In config.yml, set 'language' to '" + LanguageHandler.DEFAULT_LANGUAGE + "' or any other supported language.");
			throw new RuntimeException("Failed to load language file");
		}
	}

	@Override
	public LanguageHandler<CommandSender> getLanguageHandler() {
		return this.language;
	}

	public NamelessAPI getNamelessApi() throws NamelessException {
		return this.apiProvider.getNamelessApi();
	}

	public PapiParser getPapiParser() {
		return this.papiParser;
	}

	public net.milkbowl.vault.permission.Permission getPermissions() {
		return this.permissions;
	}

	public Economy getEconomy() {
		return this.economy;
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