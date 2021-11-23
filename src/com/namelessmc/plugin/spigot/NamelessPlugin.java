package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.ApiProvider;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.spigot.event.PlayerBan;
import com.namelessmc.plugin.spigot.event.PlayerLogin;
import com.namelessmc.plugin.spigot.event.PlayerQuit;
import com.namelessmc.plugin.spigot.hooks.PapiHook;
import com.namelessmc.plugin.spigot.hooks.PapiParser;
import com.namelessmc.plugin.spigot.hooks.PapiParserDisabled;
import com.namelessmc.plugin.spigot.hooks.PapiParserEnabled;
import com.namelessmc.plugin.spigot.hooks.PlaceholderCacher;
import com.namelessmc.plugin.spigot.hooks.maintenance.KennyMaintenance;
import com.namelessmc.plugin.spigot.hooks.maintenance.MaintenanceStatusProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class NamelessPlugin extends JavaPlugin implements CommonObjectsProvider {

	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	private static NamelessPlugin instance;
	public static NamelessPlugin getInstance() { return instance; }

	private ApiProviderImpl apiProvider;
	@Override public ApiProvider getApiProvider() { return this.apiProvider; }
	public Optional<NamelessAPI> getNamelessApi() { return this.apiProvider.getNamelessApi(); }

	private LanguageHandler language;
	@Override public LanguageHandler getLanguage() { return this.language; }

	private Permission permissions;
	public Permission getPermissions() { return this.permissions; }
	
	private PapiParser papiParser;
	public PapiParser getPapiParser() { return this.papiParser; }

	@Nullable
	private MaintenanceStatusProvider maintenanceStatusProvider;
	@Nullable public MaintenanceStatusProvider getMaintenanceStatusProvider() { return this.maintenanceStatusProvider; }

	private final @NotNull ArrayList<@NotNull BukkitTask> tasks = new ArrayList<>(2);
	private @Nullable Websend websend;

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		super.saveDefaultConfig();

		this.apiProvider = new ApiProviderImpl(this.getLogger());

		Config.initialize();

		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			final RegisteredServiceProvider<Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
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

		this.language = new LanguageHandler(getDataFolder().toPath().resolve("languages"));

		reload();

		this.initHooks();

		this.registerCommands();
		this.getServer().getPluginManager().registerEvents(new PlayerLogin(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerBan(), this);

		// For reloads
		for (final Player player : Bukkit.getOnlinePlayers()) {
			LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());
		}

		getServer().getScheduler().runTaskAsynchronously(this, this::checkUuids);

		initBstats();
	}

	@Override
	public void onDisable() {
		websend.stop();
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

	public void reload() {
		NamelessPlugin.instance.reloadConfig();
		this.apiProvider.loadConfiguration(getConfig());
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> apiProvider.getNamelessApi());

		for (final Config config : Config.values()) {
			config.reload();
		}
		try {
			this.getLanguage().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		if (!this.getLanguage().setActiveLanguage(getConfig().getString("language", LanguageHandler.DEFAULT_LANGUAGE), YamlFileImpl::new)) {
			this.getLogger().severe("LANGUAGE FILE FAILED TO LOAD");
			this.getLogger().severe("THIS IS BAD NEWS, THE PLUGIN WILL BREAK");
			this.getLogger().severe("FIX IMMEDIATELY");
			this.getLogger().severe("In config.yml, set 'language' to '" + LanguageHandler.DEFAULT_LANGUAGE + "' or any other supported language.");
			throw new RuntimeException("Failed to load language file");
		}

		for (BukkitTask task : this.tasks) {
			task.cancel();
		}

		final int rate = getConfig().getInt("server-data-upload-rate", 10) * 20;
		final int serverId = getConfig().getInt("server-id", 0);
		if (rate > 0 && serverId > 0) {
			this.tasks.add(new ServerDataSender().runTaskTimer(this, rate, rate));
		}

		final int rate2 = getConfig().getInt("user-sync.poll-interval", 0) * 20;
		if (rate2 > 0) {
			this.tasks.add(Bukkit.getScheduler().runTaskTimer(this, new UserSyncTask(), rate2, rate2));
		}

		int rate3 = getConfig().getInt("announcements.interval", 0);
		if (rate3 > 0) {
			this.tasks.add(Bukkit.getScheduler().runTaskTimer(this, new AnnouncementTask(), rate3*60*20L, rate3*60*20L));
		}

		this.tasks.trimToSize();

		if (websend != null) {
			websend.stop();
		}
		websend = new Websend(this); // this will do nothing if websend options are disabled
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
		initPapi();
		initMaintenance();
	}

	private void initPapi() {
		boolean placeholderPluginInstalled = false;

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			final PapiHook placeholders = new PapiHook();
			placeholders.register();
			placeholderPluginInstalled = true;

			this.papiParser = new PapiParserEnabled();
		} else {
			this.papiParser = new PapiParserDisabled();
		}

		if (placeholderPluginInstalled && getConfig().getBoolean("enable-placeholders", false)) {
			Bukkit.getScheduler().runTaskAsynchronously(this, new PlaceholderCacher());
		}
	}

	private void initMaintenance() {
		if (Bukkit.getPluginManager().isPluginEnabled("Maintenance")) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin("Maintenance");
			if (plugin.getDescription().getAuthors().contains("kennytv")) {
				this.maintenanceStatusProvider = new KennyMaintenance();
			}
		}
	}

	private void initBstats() {
		Metrics metrics = new Metrics(this, 13396);

		metrics.addCustomChart(new SimplePie("server_data_sender_enabled", () ->
				getConfig().getInt("server-id") > 0 &&
						getConfig().getInt("server-data-upload-rate") == 1
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("upload_placeholders_enabled", () ->
				getConfig().getBoolean("upload-placeholders.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("language", () ->
				getConfig().getString("language")));

		metrics.addCustomChart(new SimplePie("auto_ban_on_website", () ->
				getConfig().getBoolean("auto-ban-on-website")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("not_registered_join_message", () ->
				getConfig().getBoolean("not-registered-join-message")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("api_usernames_enabled", () ->
				getConfig().getBoolean("api-usernames")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_whitelist_enabled", () ->
				getConfig().getBoolean("user-sync.whitelist.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_bans_enabled", () ->
				getConfig().getBoolean("user-sync.bans.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("announcements_enabled", () ->
				getConfig().getInt("announcements.interval") > 0
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_command_executor_enabled", () ->
				getConfig().getBoolean("websend.command-executor.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_console_capture_enabled", () ->
				getConfig().getBoolean("websend.console-capture.enabled")
						? "Enabled" : "Disabled"));
	}

	public static void log(final Level level, final String message) {
		NamelessPlugin.getInstance().getLogger().log(level, message);
	}

}
