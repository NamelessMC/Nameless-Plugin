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
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.spigot.event.PlayerLogin;
import com.namelessmc.plugin.spigot.event.PlayerQuit;
import com.namelessmc.plugin.spigot.hooks.PapiHook;
import com.namelessmc.plugin.spigot.hooks.PapiParser;
import com.namelessmc.plugin.spigot.hooks.PapiParserDisabled;
import com.namelessmc.plugin.spigot.hooks.PapiParserEnabled;
import com.namelessmc.plugin.spigot.hooks.PlaceholderCacher;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;

public class NamelessPlugin extends JavaPlugin implements CommonObjectsProvider {

	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	private static NamelessPlugin instance;

	private ApiProviderImpl apiProvider;
	private LanguageHandler language;
	private static BukkitAudiences adventure;

	private net.milkbowl.vault.permission.Permission permissions;
	private PapiParser papiParser;
	private BukkitTask dataSenderTask;

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		this.apiProvider = new ApiProviderImpl();

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
		} else {
			log(Level.WARNING, "Vault was not found. Group sync will not work.");
		}

		adventure = BukkitAudiences.create(this);

		this.language = new LanguageHandler(getDataFolder().toPath().resolve("languages"));

		reload();

		this.initHooks();

		// Connection is successful, move on with registering listeners and commands.
		this.registerCommands();
		this.getServer().getPluginManager().registerEvents(new PlayerLogin(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerQuit(), this);

		// Start saving data files every 15 minutes
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveConfig(), 5*60*20, 5*60*20);

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
			this.getLanguage().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		if (!this.getLanguage().setActiveLanguage(Config.MAIN.getConfig().getString("language", LanguageHandler.DEFAULT_LANGUAGE), YamlFileImpl::new)) {
			this.getLogger().severe("LANGUAGE FILE FAILED TO LOAD");
			this.getLogger().severe("THIS IS BAD NEWS, THE PLUGIN WILL BREAK");
			this.getLogger().severe("FIX IMMEDIATELY");
			this.getLogger().severe("In config.yml, set 'language' to '" + LanguageHandler.DEFAULT_LANGUAGE + "' or any other supported language.");
			throw new RuntimeException("Failed to load language file");
		}

		if (this.dataSenderTask != null) {
			this.dataSenderTask.cancel();
		}

		final int rate = Config.MAIN.getConfig().getInt("server-data-upload-rate", 10) * 20;
		final int serverId = Config.MAIN.getConfig().getInt("server-id");
		if (rate <= 0 || serverId <= 0) {
			this.dataSenderTask = null;
		} else {
			this.dataSenderTask = new ServerDataSender().runTaskTimer(this, rate, rate);
		}
	}

	@Override
	public NamelessAPI getNamelessApi() throws NamelessException {
		return this.apiProvider.getNamelessApi();
	}

	@Override
	public AbstractScheduler getScheduler() {
		return new AbstractScheduler() {

			@Override
			public void runAsync(final Runnable runnable) {
				Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.this, runnable);
			}

			@Override
			public void runSync(final Runnable runnable) {
				Bukkit.getScheduler().runTask(NamelessPlugin.this, runnable);
			}

		};
	}

	@Override
	public LanguageHandler getLanguage() {
		return this.language;
	}

	@Override
	public BukkitAudiences adventure() {
		return adventure;
	}

	public PapiParser getPapiParser() {
		return this.papiParser;
	}

	public net.milkbowl.vault.permission.Permission getPermissions() {
		return this.permissions;
	}

	private void registerCommands() {
		this.getServer().getPluginCommand("namelessplugin").setExecutor(new PluginCommand());

		try {
			final Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			field.setAccessible(true);
			final CommandMap map = (CommandMap) field.get(Bukkit.getServer());

			for (final String name : CommonCommandProxy.COMMAND_SUPPLIERS.keySet()) {
				if (Config.COMMANDS.getConfig().contains(name)) {
					map.register(this.getName(), CommonCommandProxy.COMMAND_SUPPLIERS.get(name).get());
				}
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