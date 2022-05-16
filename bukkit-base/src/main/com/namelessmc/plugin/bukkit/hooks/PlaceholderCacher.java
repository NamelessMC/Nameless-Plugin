package com.namelessmc.plugin.bukkit.hooks;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaceholderCacher implements Listener, Reloadable {


	private final @NonNull BukkitNamelessPlugin bukkitPlugin;
	private final @NonNull NamelessPlugin plugin;
	private @Nullable AbstractScheduledTask task;
	private @Nullable AtomicBoolean isRunning;
	private @Nullable Map<UUID, Integer> cachedNotificationCount;

	public PlaceholderCacher(final @NonNull BukkitNamelessPlugin bukkitPlugin,
							 final @NonNull NamelessPlugin plugin) {
		this.bukkitPlugin = bukkitPlugin;
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

		final Configuration config = this.plugin.config().main();
		if (config.getBoolean("retrieve-placeholders.enabled")) {
			Bukkit.getPluginManager().registerEvents(this, this.bukkitPlugin);
			Duration interval = Duration.parse(config.getString("retrieve-placeholders.interval"));
			this.task = this.plugin.scheduler().runTimer(this::updateCache, interval);
			this.isRunning = new AtomicBoolean();
			this.cachedNotificationCount = new HashMap<>();
		}
	}

	private void updateCache() {
		if (isRunning == null) {
			throw new IllegalStateException("Placeholder cacher is disabled");
		}

		if (isRunning.compareAndSet(false, true)) {
			final Optional<NamelessAPI> optApi = this.plugin.apiProvider().api();
			if (optApi.isPresent()) {
				final NamelessAPI api = optApi.get();
				for (final Player player : Bukkit.getOnlinePlayers()) {
					updateCache(api, player);
				}
			}
			isRunning.set(false);
		}
	}

	private void updateCache(final @NonNull NamelessAPI api, final @NonNull Player player) {
		if (this.cachedNotificationCount == null) {
			throw new IllegalStateException("Placeholder cacher is disabled");
		}

		try {
			final Optional<NamelessUser> user = api.getUserByMinecraftUuid(player.getUniqueId());
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
	public void onJoin(final PlayerQuitEvent event) {
		if (cachedNotificationCount == null) {
			this.plugin.logger().severe("On join event called while placeholder cacher disabled");
			return;
		}

		this.cachedNotificationCount.remove(event.getPlayer().getUniqueId());

		final Optional<NamelessAPI> optApi = this.plugin.apiProvider().api();
		optApi.ifPresent(api -> this.plugin.scheduler().runAsync(() -> updateCache(api, event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(final PlayerQuitEvent event) {
		if (cachedNotificationCount == null) {
			this.plugin.logger().severe("On quit event called while placeholder cacher disabled");
			return;
		}

		this.cachedNotificationCount.remove(event.getPlayer().getUniqueId());
	}

	public int getNotificationCount(final @NonNull OfflinePlayer player) {
		if (this.cachedNotificationCount == null) {
			return -1;
		}

		Integer count = this.cachedNotificationCount.get(player.getUniqueId());
		return count != null ? count : -1;
	}

}
