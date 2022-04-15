package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.JulLogger;
import com.namelessmc.plugin.spigot.event.PlayerBan;
import com.namelessmc.plugin.spigot.event.PlayerLogin;
import com.namelessmc.plugin.spigot.hooks.*;
import com.namelessmc.plugin.spigot.hooks.maintenance.KennyMaintenance;
import com.namelessmc.plugin.spigot.hooks.maintenance.MaintenanceStatusProvider;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class NamelessPluginSpigot extends JavaPlugin {

	private Permission permissions;
	public Permission getPermissions() { return this.permissions; }
	
	private PapiParser papiParser;
	public PapiParser getPapiParser() { return this.papiParser; }

	private @Nullable MaintenanceStatusProvider maintenanceStatusProvider;
	public @Nullable MaintenanceStatusProvider getMaintenanceStatusProvider() { return this.maintenanceStatusProvider; }

	private final @NotNull NamelessPlugin plugin;

	public NamelessPluginSpigot() {
		final Path dataDirectory = this.getDataFolder().toPath();
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new SpigotScheduler(this),
				config -> new JulLogger(config, this.getLogger())
		);
		this.plugin.registerReloadable(new SpigotCommandProxy(this.plugin));
		this.plugin.registerReloadable(new SpigotDataSender(this.plugin, this));
		this.plugin.registerReloadable(new UserSyncTask(this.plugin));
		this.plugin.registerReloadable(new AnnouncementTask(this.plugin));
		PapiHook.cacher = this.plugin.registerReloadable(
				new PlaceholderCacher(this, this.plugin)
		);
	}

	@Override
	public void onEnable() {
		this.plugin.setAudienceProvider(new SpigotAudienceProvider(this));

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

		this.plugin.reload();

		initPapi();
		initMaintenance();
		initMetrics();

		this.getServer().getPluginManager().registerEvents(new PlayerLogin(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerBan(), this);

		this.getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onJoin(final PlayerJoinEvent event) {
				final Player player = event.getPlayer();
				plugin.onJoin(plugin.audiences().player(player.getUniqueId()));
			}
			@EventHandler
			public void onQuit(final PlayerQuitEvent event) {
				plugin.onQuit(event.getPlayer().getUniqueId());
			}
		}, this);

		getServer().getScheduler().runTaskAsynchronously(this, this::checkUuids);
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
	}

}
