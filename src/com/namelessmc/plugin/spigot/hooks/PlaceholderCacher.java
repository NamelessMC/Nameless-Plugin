package com.namelessmc.plugin.spigot.hooks;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.spigot.NamelessPluginSpigot;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaceholderCacher implements Listener, Reloadable {


	private final @NotNull NamelessPluginSpigot spigotPlugin;
	private final @NotNull NamelessPlugin plugin;
	private AbstractScheduledTask task;
	private AtomicBoolean isRunning;
	private Map<UUID, Integer> cachedNotificationCount;

	public PlaceholderCacher(final @NotNull NamelessPluginSpigot spigotPlugin,
							 final @NotNull NamelessPlugin plugin) {
		this.spigotPlugin = spigotPlugin;
		this.plugin = plugin;
	}

	public void reload() {
		HandlerList.unregisterAll(this);
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
			this.isRunning = null;
			this.cachedNotificationCount = null;
		}

		final Configuration config = this.plugin.config().getMainConfig();
		if (config.getBoolean("retrieve-placeholders.enabled")) {
			Bukkit.getPluginManager().registerEvents(this, spigotPlugin);
			Duration interval = Duration.parse(config.getString("retrieve-placeholders.interval"));
			this.task = this.plugin.scheduler().runTimer(this::updateCache, interval);
			this.isRunning = new AtomicBoolean();
			this.cachedNotificationCount = new HashMap<>();
		}
	}

	private void updateCache() {
		if (isRunning.compareAndSet(false, true)) {
			final Optional<NamelessAPI> optApi = this.plugin.api().getNamelessApi();
			if (optApi.isPresent()) {
				final NamelessAPI api = optApi.get();
				for (final Player player : Bukkit.getOnlinePlayers()) {
					updateCache(api, player);
				}
			}
			isRunning.set(false);
		}
	}

	private void updateCache(NamelessAPI api, Player player) {
		try {
			final Optional<NamelessUser> user = this.plugin.api().userFromPlayer(api, player);
			if (user.isEmpty()) {
				return;
			}
			final int notificationCount = user.get().getNotificationCount();
			this.cachedNotificationCount.put(player.getUniqueId(), notificationCount);
		} catch (final NamelessException e) {
			this.plugin.logger().logException(e);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerQuitEvent event) {
		this.cachedNotificationCount.remove(event.getPlayer().getUniqueId());

		final Optional<NamelessAPI> optApi = this.plugin.api().getNamelessApi();
		optApi.ifPresent(api -> this.plugin.scheduler().runAsync(() -> updateCache(api, event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		this.cachedNotificationCount.remove(event.getPlayer().getUniqueId());
	}

	public void stop() {
		task.cancel();
		HandlerList.unregisterAll(this);
	}

	public int getNotificationCount(@NotNull OfflinePlayer player) {
		Integer count = this.cachedNotificationCount.get(player.getUniqueId());
		return count != null ? count : -1;
	}

}
