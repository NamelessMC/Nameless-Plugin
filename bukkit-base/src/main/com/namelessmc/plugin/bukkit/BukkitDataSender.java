package com.namelessmc.plugin.bukkit;

import com.google.gson.JsonObject;
import com.namelessmc.plugin.bukkit.hooks.maintenance.MaintenanceStatusProvider;
import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public class BukkitDataSender extends AbstractDataSender {

	private final @NonNull NamelessPlugin plugin;
	private final @NonNull BukkitNamelessPlugin spigotPlugin;

	protected BukkitDataSender(final @NonNull NamelessPlugin plugin,
							   final @NonNull BukkitNamelessPlugin spigotPlugin) {
		super(plugin);
		this.plugin = plugin;
		this.spigotPlugin = spigotPlugin;
	}

	@Override
	protected void registerCustomProviders() {
		final Configuration config = this.getPlugin().config().main();

		// TPS TODO Send real TPS
		this.registerGlobalInfoProvider(json ->
				json.addProperty("tps", 20));

		// Permissions
		final VaultPermissions permissions = VaultPermissions.create(plugin);
		if (permissions != null) {
			this.registerGlobalInfoProvider(permissions);
			this.registerPlayerInfoProvider(permissions);
		}

		// Maintenance
		MaintenanceStatusProvider maintenance = spigotPlugin.getMaintenanceStatusProvider();
		if (maintenance != null) {
			this.registerGlobalInfoProvider(json ->
					json.addProperty("maintenance", maintenance.maintenanceEnabled()));
		}

		final boolean uploadPlaceholders = config.getBoolean("server-data-sender.placeholders.enabled");

		// PlaceholderAPI placeholders
		if (uploadPlaceholders) {
			this.registerGlobalInfoProvider(json -> {
				final JsonObject placeholders = new JsonObject();
				config.getStringList("server-data-sender.placeholders.global").forEach((key) ->
						placeholders.addProperty(key, ChatColor.stripColor(spigotPlugin.getPapiParser().parse(null, "%" + key + "%"))));
				json.add("placeholders", placeholders);
			});
			this.registerPlayerInfoProvider((json, player) -> {
				final Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
				final JsonObject placeholders = new JsonObject();
				config.getStringList("server-data-sender.placeholders.player").forEach((key) ->
						placeholders.addProperty(key, ChatColor.stripColor(spigotPlugin.getPapiParser().parse(bukkitPlayer, "%" + key + "%"))));
				json.add("placeholders", placeholders);
			});

		}

		// Location
		this.registerPlayerInfoProvider((json, player) -> {
			final Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
			final JsonObject location = new JsonObject();
			final Location loc = bukkitPlayer.getLocation();
			location.addProperty("world", loc.getWorld().getName());
			location.addProperty("x", loc.getBlockX());
			location.addProperty("y", loc.getBlockY());
			location.addProperty("z", loc.getBlockZ());
			json.add("location", location);
		});

		Statistic playStat;
		try {
			playStat = Statistic.PLAY_ONE_TICK;
		} catch (final NoSuchFieldError ignored) {
			try {
				// it's PLAY_ONE_MINUTE in 1.13+ but unlike the name suggests it actually still records ticks played
				//noinspection JavaReflectionMemberAccess
				playStat = (Statistic) Statistic.class.getField("PLAY_ONE_MINUTE").get(null);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
					 | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		final Statistic finalPlayStat = Objects.requireNonNull(playStat);

		// Misc player stats
		this.registerPlayerInfoProvider((json, player) -> {
			final Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
			json.addProperty("playtime", bukkitPlayer.getStatistic(finalPlayStat) / 120);
			json.addProperty("ip", bukkitPlayer.getAddress().toString());
		});
	}

}
