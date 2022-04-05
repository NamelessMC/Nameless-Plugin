package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.JulLogger;
import com.namelessmc.plugin.spigot.event.PlayerBan;
import com.namelessmc.plugin.spigot.event.PlayerLogin;
import com.namelessmc.plugin.spigot.event.PlayerQuit;
import com.namelessmc.plugin.spigot.hooks.*;
import com.namelessmc.plugin.spigot.hooks.maintenance.KennyMaintenance;
import com.namelessmc.plugin.spigot.hooks.maintenance.MaintenanceStatusProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.md_5.bungee.config.Configuration;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NamelessPluginSpigot extends JavaPlugin {

	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	private Permission permissions;
	public Permission getPermissions() { return this.permissions; }
	
	private PapiParser papiParser;
	public PapiParser getPapiParser() { return this.papiParser; }

	private BukkitAudiences adventure;
	public BukkitAudiences adventure() { return adventure; }

	private @Nullable MaintenanceStatusProvider maintenanceStatusProvider;
	public @Nullable MaintenanceStatusProvider getMaintenanceStatusProvider() { return this.maintenanceStatusProvider; }
	
	private final @NotNull ArrayList<@NotNull BukkitTask> tasks = new ArrayList<>(2);

	private final @NotNull NamelessPlugin plugin;
	private @Nullable Websend websend;

	public NamelessPluginSpigot() {
		final Path dataDirectory = this.getDataFolder().toPath();
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new SpigotScheduler(this),
				config -> new JulLogger(config, this.getLogger())
		);
		this.plugin.registerReloadable(new SpigotCommandProxy(this.plugin, this));
		this.plugin.registerReloadable(new ServerDataSender(this, this.plugin));
		this.plugin.registerReloadable(new UserSyncTask(this.plugin));
		this.plugin.registerReloadable(new AnnouncementTask(this.plugin));
		PapiHook.cacher = this.plugin.registerReloadable(
				new PlaceholderCacher(this, this.plugin)
		);
	}

	@Override
	public void onEnable() {
		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			final RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider == null) {
				this.plugin.logger().warning("No vault compatible permissions plugin was found. Group sync will not work.");
			} else {
				this.permissions = permissionProvider.getProvider();

				if (this.permissions == null) {
					this.plugin.logger().warning("No vault compatible permissions plugin was found. Group sync will not work.");
				}
			}
		} else {
			this.plugin.logger().warning("Vault was not found. Group sync will not work.");
		}

		adventure = BukkitAudiences.create(this);

		initPapi();
		initMaintenance();

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

	// TODO make this work for all platforms
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
	
	private void initPapi() {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			final PapiHook placeholders = new PapiHook();
			placeholders.register();

			this.papiParser = new PapiParserEnabled();
		} else {
			this.papiParser = new PapiParserDisabled();
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
		// TODO make this cross-platform
		Metrics metrics = new Metrics(this, 13396);

		Configuration config = this.plugin.config().getMainConfig();
		metrics.addCustomChart(new SimplePie("server_data_sender_enabled", () ->
				config.getInt("server-id") > 0 &&
						config.getInt("server-data-upload-rate") == 1
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("upload_placeholders_enabled", () ->
				config.getBoolean("upload-placeholders.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("language", () ->
				config.getString("language")));

		metrics.addCustomChart(new SimplePie("auto_ban_on_website", () ->
				config.getBoolean("auto-ban-on-website")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("not_registered_join_message", () ->
				config.getBoolean("not-registered-join-message")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("api_usernames_enabled", () ->
				config.getBoolean("api-usernames")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_whitelist_enabled", () ->
				config.getBoolean("user-sync.whitelist.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_bans_enabled", () ->
				config.getBoolean("user-sync.bans.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("announcements_enabled", () ->
				config.getInt("announcements.interval") > 0
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_command_executor_enabled", () ->
				config.getBoolean("websend.command-executor.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_console_capture_enabled", () ->
				config.getBoolean("websend.console-capture.enabled")
						? "Enabled" : "Disabled"));
	}

}
