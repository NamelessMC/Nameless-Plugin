package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.bukkit.hooks.PapiHook;
import com.namelessmc.plugin.bukkit.hooks.PapiWrapper;
import com.namelessmc.plugin.bukkit.hooks.PlaceholderCacher;
import com.namelessmc.plugin.bukkit.hooks.maintenance.KennyMaintenance;
import com.namelessmc.plugin.bukkit.hooks.maintenance.MaintenanceStatusProvider;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.JulLogger;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;

public abstract class BukkitNamelessPlugin extends JavaPlugin {
	
	private @Nullable PapiWrapper papiWrapper;
	public @Nullable PapiWrapper papiWrapper() { return this.papiWrapper; }

	private @Nullable MaintenanceStatusProvider maintenanceStatusProvider;
	public @Nullable MaintenanceStatusProvider getMaintenanceStatusProvider() { return this.maintenanceStatusProvider; }

	protected final @NonNull NamelessPlugin plugin;

	private final @NonNull PlaceholderCacher placeholderCacher;

	public BukkitNamelessPlugin(String platformInternalName) {
		final Path dataDirectory = this.getDataFolder().toPath();
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new BukkitScheduler(this),
				config -> new JulLogger(config, this.getLogger()),
				Path.of("logs", "latest.log"),
				platformInternalName,
				Bukkit.getVersion()
		);
		this.plugin.registerReloadable(new BukkitDataSender(this.plugin, this));
		this.plugin.registerReloadable(new UserSyncTask(this.plugin, this));
		this.placeholderCacher = this.plugin.registerReloadable(
				new PlaceholderCacher(this, this.plugin)
		);
	}

	@Override
	public void onEnable() {
		this.configureAudiences();

		this.plugin.reload();

		initPapi();
		initMaintenance();
		new Metrics(this, 13396);

		BukkitCommandProxy.registerCommands(this.plugin, this);

		this.getServer().getPluginManager().registerEvents(new BukkitEventProxy(this.plugin), this);
	}

	protected abstract void configureAudiences();

	public abstract void kickPlayer(final @NonNull Player player, final LanguageHandler.@NonNull Term term);
	
	private void initPapi() {
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			final PapiHook placeholders = new PapiHook(this.placeholderCacher);
			placeholders.register();
			this.papiWrapper = new PapiWrapper();
		} else {
			this.papiWrapper = null;
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

}
