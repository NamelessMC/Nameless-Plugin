package com.namelessmc.plugin.spigot.hooks;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaceholderCacher implements Listener {

	private final Map<UUID, Integer> CACHED_NOTIFICATION_COUNT = new HashMap<>();
	private final BukkitTask task;
	private final AtomicBoolean isRunning = new AtomicBoolean();

	public PlaceholderCacher(NamelessPlugin plugin, long updateInterval) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateCache, updateInterval, updateInterval);
	}

	private void updateCache() {
		if (isRunning.compareAndSet(false, true)) {
			final Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
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
			final Optional<NamelessUser> user = NamelessPlugin.getInstance().getApiProvider().userFromPlayer(api, player);
			if (!user.isPresent()) {
				return;
			}
			final int notificationCount = user.get().getNotificationCount();
			CACHED_NOTIFICATION_COUNT.put(player.getUniqueId(), notificationCount);
		} catch (final NamelessException e) {
			NamelessPlugin.getInstance().getExceptionLogger().logException(e);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerQuitEvent event) {
		CACHED_NOTIFICATION_COUNT.remove(event.getPlayer().getUniqueId());

		final Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
		optApi.ifPresent(api -> {
			Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
				updateCache(api, event.getPlayer());
			});
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		CACHED_NOTIFICATION_COUNT.remove(event.getPlayer().getUniqueId());
	}

	public void stop() {
		task.cancel();
		HandlerList.unregisterAll(this);
	}

	public int getNotificationCount(@NotNull OfflinePlayer player) {
		Integer count = CACHED_NOTIFICATION_COUNT.get(player.getUniqueId());
		return count != null ? count : -1;
	}

}
