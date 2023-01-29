package com.namelessmc.plugin.bukkit;

import com.google.gson.JsonObject;
import com.namelessmc.plugin.bukkit.hooks.PapiHook;
import com.namelessmc.plugin.bukkit.hooks.maintenance.MaintenanceStatusProvider;
import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

public class BukkitDataSender extends AbstractDataSender {

	private final @NonNull NamelessPlugin plugin;
	private final @NonNull BukkitNamelessPlugin bukkitPlugin;

	protected BukkitDataSender(final @NonNull NamelessPlugin plugin,
							   final @NonNull BukkitNamelessPlugin bukkitPlugin) {
		super(plugin);
		this.plugin = plugin;
		this.bukkitPlugin = bukkitPlugin;
	}

	@Override
	protected void registerCustomProviders() {
		// Max players
		this.registerGlobalInfoProvider(json ->
				json.addProperty("max_players", Bukkit.getServer().getMaxPlayers()));

		// Motd
		this.registerGlobalInfoProvider(json -> {
			final InetAddress address = new InetSocketAddress("nameless-fake-ping", 1234).getAddress();
			final String motd = Bukkit.getServer().getMotd();
			final int onlinePlayers = Bukkit.getServer().getOnlinePlayers().size();
			final int maxPlayers = Bukkit.getServer().getMaxPlayers();
			// Send fake ping event so plugins can change the motd
			final ServerListPingEvent event = new ServerListPingEvent(address, motd, onlinePlayers, maxPlayers);
			Bukkit.getPluginManager().callEvent(event);
			json.addProperty("motd", event.getMotd());
		});

		// Maintenance
		MaintenanceStatusProvider maintenance = this.bukkitPlugin.getMaintenanceStatusProvider();
		if (maintenance != null) {
			this.registerGlobalInfoProvider(json ->
					json.addProperty("maintenance", maintenance.maintenanceEnabled()));
		}

		// PlaceholderAPI placeholders
		{
			final ConfigurationNode config = this.getPlugin().config().main().node("server-data-sender", "placeholders");
			if (config.node("enabled").getBoolean()) {
				PapiHook papi = this.bukkitPlugin.papi();

				this.registerGlobalInfoProvider(json -> {
					try {
						final JsonObject placeholders = new JsonObject();
						config.node("global").getList(String.class).forEach((key) ->
								placeholders.addProperty(key, ChatColor.stripColor(papi.parse(null, "%" + key + "%"))));
						json.add("placeholders", placeholders);
					} catch (SerializationException e) {
						this.plugin.logger().warning("Invalid global placeholders list");
					}
				});
				this.registerPlayerInfoProvider((json, player) -> {
					try {
						final Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
						final JsonObject placeholders = new JsonObject();
						config.node("player").getList(String.class).forEach((key) ->
								placeholders.addProperty(key, ChatColor.stripColor(papi.parse(bukkitPlayer, "%" + key + "%"))));
						json.add("placeholders", placeholders);
					} catch (SerializationException e) {
						this.plugin.logger().warning("Invalid player placeholders list");
					}
				});
			}
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
