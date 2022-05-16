package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.bukkit.event.PlayerBan;
import com.namelessmc.plugin.bukkit.hooks.*;
import com.namelessmc.plugin.bukkit.hooks.maintenance.KennyMaintenance;
import com.namelessmc.plugin.bukkit.hooks.maintenance.MaintenanceStatusProvider;
import com.namelessmc.plugin.common.AnnouncementTask;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.JulLogger;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;

public abstract class BukkitNamelessPlugin extends JavaPlugin {
	
	private PapiParser papiParser;
	public PapiParser getPapiParser() { return this.papiParser; }

	private @Nullable MaintenanceStatusProvider maintenanceStatusProvider;
	public @Nullable MaintenanceStatusProvider getMaintenanceStatusProvider() { return this.maintenanceStatusProvider; }

	protected final @NonNull NamelessPlugin plugin;

	private @Nullable Boolean usesMojangUuids;

	private final @NonNull PlaceholderCacher placeholderCacher;

	public BukkitNamelessPlugin() {
		final Path dataDirectory = this.getDataFolder().toPath();
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new BukkitScheduler(this),
				config -> new JulLogger(config, this.getLogger())
		);
		this.plugin.registerReloadable(new BukkitCommandProxy(this.plugin));
		this.plugin.registerReloadable(new BukkitDataSender(this.plugin, this));
		this.plugin.registerReloadable(new UserSyncTask(this.plugin, this));
		this.placeholderCacher = this.plugin.registerReloadable(
				new PlaceholderCacher(this, this.plugin)
		);
		this.plugin.registerReloadable(new Websend(this.plugin));
	}

	@Override
	public void onEnable() {
		this.configureAudiences();

		this.plugin.reload();

		initPapi();
		initMaintenance();
		initMetrics();

		this.getServer().getPluginManager().registerEvents(new PlayerBan(), this);
		this.getServer().getPluginManager().registerEvents(new BukkitEventProxy(this.plugin), this);

		getServer().getScheduler().runTaskAsynchronously(this, this::checkUuids);
	}

	protected abstract void configureAudiences();

	public abstract void kickPlayer(final @NonNull Player player, final LanguageHandler.@NonNull Term term);

	private void checkUuids() {
		@SuppressWarnings("deprecation")
		final OfflinePlayer notch = Bukkit.getOfflinePlayer("Notch");
		this.usesMojangUuids = notch.getUniqueId().toString().equals("069a79f4-44e9-4726-a5be-fca90e38aaf5");
		if (!usesMojangUuids) {
			getLogger().severe("*** IMPORTANT ***");
			getLogger().severe("Your server does not use Mojang UUIDs!");
			getLogger().severe("This plugin won't work for cracked servers. If you do not intend to run a cracked server and you use BungeeCord, make sure `bungeecord: true` is set in spigot.yml and ip forwarding is enabled in the BungeeCord config file.");
		}
	}
	
	private void initPapi() {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			final PapiHook placeholders = new PapiHook(this.placeholderCacher);
			placeholders.register();

			this.papiParser = new PapiParserEnabled();
		} else {
			this.papiParser = new PapiParserDisabled();
		}
	}
	
	private void initMaintenance() {
		if (!Bukkit.getPluginManager().isPluginEnabled("Maintenance")) {
			return;
		}

		Plugin plugin = Bukkit.getPluginManager().getPlugin("Maintenance");
		if (!plugin.getDescription().getAuthors().contains("kennytv")) {
			return;
		}

		final String version = plugin.getDescription().getVersion();
		if (!version.startsWith("4")) {
			this.plugin.logger().warning("Ignoring unsupported KennyTV Maintenance version: " +
					version);
			return;
		}

		this.maintenanceStatusProvider = new KennyMaintenance();
	}

	private void initMetrics() {
		Metrics metrics = new Metrics(this, 13396);
		this.plugin.registerCustomCharts(metrics, Metrics.class);

		metrics.addCustomChart(new SimplePie("mojang_uuids", () ->
				this.usesMojangUuids == null ? "Unknown" : (
						this.usesMojangUuids ? "Yes" : "No")));
	}

}
