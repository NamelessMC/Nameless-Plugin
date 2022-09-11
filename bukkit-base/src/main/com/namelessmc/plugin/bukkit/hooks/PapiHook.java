package com.namelessmc.plugin.bukkit.hooks;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.NamelessModule;
import com.namelessmc.java_api.modules.store.PaymentsFilter;
import com.namelessmc.java_api.modules.store.StorePayment;
import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.MavenConstants;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class PapiHook implements Reloadable, Listener {

	private final BukkitNamelessPlugin bukkitPlugin;
	private final NamelessPlugin plugin;
	private @Nullable AbstractScheduledTask task;
	private @Nullable AtomicBoolean isRunning;
	private @Nullable Expansion placeholderExpansion;
	private BiFunction<Player, String, String> placeholderParser = new NoopParser();

	private @Nullable Map<UUID, Integer> cachedNotificationCount;
	private @Nullable Map<UUID, Integer> cachedStoreCredits;
	private @Nullable StorePayment cachedLastStorePayment;

	public PapiHook(final @NonNull BukkitNamelessPlugin bukkitPlugin,
							 final @NonNull NamelessPlugin plugin) {
		this.bukkitPlugin = bukkitPlugin;
		this.plugin = plugin;
	}

	@Override
	public void unload() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}

		this.isRunning = null;
		this.cachedNotificationCount = null;
		this.cachedStoreCredits = null;
		this.cachedLastStorePayment = null;
		HandlerList.unregisterAll(this);

		if (this.placeholderExpansion != null) {
			this.placeholderExpansion.unregister();
			this.placeholderExpansion = null;
		}
	}

	@Override
	public void load() {
		if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			this.plugin.logger().fine("PlaceholderAPI plugin is not loaded");
			this.placeholderParser = new NoopParser();
			return;
		}

		this.placeholderParser = PlaceholderAPI::setPlaceholders;

		final CommentedConfigurationNode config = this.plugin.config().main().node("retrieve-placeholders");
		if (config.node("enabled").getBoolean()) {
			final Expansion expansion = new Expansion();
			expansion.register();

			Bukkit.getPluginManager().registerEvents(this, this.bukkitPlugin);

			final Duration interval = ConfigurationHandler.getDuration(config.node("interval"));
			if (interval == null) {
				this.plugin.logger().warning("Placeholder caching interval invalid");
				return;
			}
			this.task = this.plugin.scheduler().runTimer(this::updateCache, interval);
			this.plugin.scheduler().runAsync(this::updateCache);
			this.isRunning = new AtomicBoolean();
			this.cachedNotificationCount = new ConcurrentHashMap<>();
			this.cachedStoreCredits = new ConcurrentHashMap<>();
		}
	}

	private void updateCache() {
		if (isRunning == null) {
			throw new IllegalStateException("Placeholder caching is disabled");
		}

		final Set<UUID> uuids = Bukkit.getOnlinePlayers().stream()
				.map(OfflinePlayer::getUniqueId)
				.collect(Collectors.toUnmodifiableSet());

		this.plugin.scheduler().runAsync(() -> {
			if (isRunning.compareAndSet(false, true)) {
				try {
					final NamelessAPI api = this.plugin.apiProvider().api();
					if (api == null) {
						this.plugin.logger().fine("Skipped placeholder caching, API connection is broken");
						return;
					}

					if (api.website().modules().contains(NamelessModule.STORE)) {
						List<StorePayment> payments = api.store().payments(PaymentsFilter.limit(1));
						if (payments.isEmpty()) {
							this.cachedLastStorePayment = null;
						} else if (payments.size() == 1) {
							this.cachedLastStorePayment = payments.get(0);
						} else {
							throw new IllegalStateException(String.valueOf(payments.size()));
						}
					}

					for (final UUID uuid : uuids) {
						updatePlayerCache(api, uuid);
					}
				} catch (final NamelessException e) {
					this.plugin.logger().logException(e);
				}
				isRunning.set(false);
			}
		});
	}

	private void updatePlayerCache(final NamelessAPI api, final UUID uuid) throws NamelessException {
		if (this.cachedNotificationCount == null ||
				this.cachedStoreCredits == null) {
			throw new IllegalStateException("Placeholder caching is disabled");
		}

		this.plugin.logger().fine(() -> "Updating notification count placeholder for " + uuid);
		final NamelessUser user = api.userByMinecraftUuid(uuid);
		if (user != null) {
			this.cachedNotificationCount.put(uuid, user.notificationCount());
			if (api.website().modules().contains(NamelessModule.STORE)) {
				this.cachedStoreCredits.put(uuid, user.store().creditsCents());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(final PlayerQuitEvent event) {
		if (cachedNotificationCount == null) {
			this.plugin.logger().severe("On join event called while placeholder caching disabled");
			return;
		}

		this.cachedNotificationCount.remove(event.getPlayer().getUniqueId());

		this.plugin.scheduler().runAsync(() -> {
			final NamelessAPI api = this.plugin.apiProvider().api();
			if (api != null) {
				try {
					updatePlayerCache(api, event.getPlayer().getUniqueId());
				} catch (NamelessException e) {
					this.plugin.logger().logException(e);
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(final PlayerQuitEvent event) {
		if (cachedNotificationCount == null) {
			this.plugin.logger().severe("On quit event called while placeholder caching disabled");
			return;
		}

		this.cachedNotificationCount.remove(event.getPlayer().getUniqueId());
	}

	public String parse(final Player player, final String text) {
		return this.placeholderParser.apply(player, text);
	}

	private class Expansion extends PlaceholderExpansion {

		@Override
		public @Nullable String onPlaceholderRequest(final Player player, final String identifier) {
			switch(identifier) {
				case "notifications":
					if (player == null ||
							cachedNotificationCount == null ||
							!cachedNotificationCount.containsKey(player.getUniqueId())
						) {
						return "?";
					} else {
						return String.valueOf(cachedNotificationCount.get(player.getUniqueId()));
					}
				case "store_last_payment_received_username":
					return cachedLastStorePayment == null ? "" : cachedLastStorePayment.receivingCustomer().username();
				case "store_last_payment_paid_username":
					return cachedLastStorePayment == null ? "" : cachedLastStorePayment.payingCustomer().username();
				case "store_last_payment_amount":
					return cachedLastStorePayment == null ? "" : cachedLastStorePayment.amount();
				case "store_last_payment_currency":
					return cachedLastStorePayment == null ? "" : cachedLastStorePayment.currency();
				case "store_last_payment_date":
					return cachedLastStorePayment == null ? "" : plugin.dateFormatter().format(cachedLastStorePayment.creationDate());
				case "store_credits":
					if (player == null ||
							cachedStoreCredits == null ||
							!cachedStoreCredits.containsKey(player.getUniqueId())) {
						return "";
					} else {
						return String.format("%.2f", cachedStoreCredits.get(player.getUniqueId()) / 100f);
					}
				default:
					return null;
			}
		}

		@Override
		public @NonNull String getAuthor() {
			return "Derkades";
		}

		@Override
		public @NonNull String getIdentifier() {
			return "nameless";
		}

		@Override
		public @NonNull String getVersion() {
			return MavenConstants.PROJECT_VERSION;
		}

		@Override
		public boolean persist() {
			return true;
		}

		@Override
		public boolean canRegister() {
			return true;
		}

	}

	private class NoopParser implements BiFunction<Player, String, String> {

		@Override
		public String apply(Player player, String text) {
			PapiHook.this.plugin.logger().warning("Attempted to parse placeholder %" + text + "% but PlaceholderAPI integration is not working");
			return text;
		}

	}

}
