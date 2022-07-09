package com.namelessmc.plugin.bukkit;

import com.namelessmc.java_api.*;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.integrations.StandardIntegrationTypes;
import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

import static com.namelessmc.plugin.common.LanguageHandler.Term.USER_SYNC_KICK;

public class UserSyncTask implements Runnable, Reloadable {

	private final @NonNull NamelessPlugin plugin;
	private final @NonNull BukkitNamelessPlugin bukkitPlugin;
	private @Nullable AbstractScheduledTask task;

	UserSyncTask(final @NonNull NamelessPlugin plugin, final @NonNull BukkitNamelessPlugin bukkitPlugin) {
		this.plugin = plugin;
		this.bukkitPlugin = bukkitPlugin;
	}

	@Override
	public void unload() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	@Override
	public void load() {
		final CommentedConfigurationNode config = this.plugin.config().main().node("user-sync");
		if (config.node("enabled").getBoolean()) {
			final Duration interval = ConfigurationHandler.getDuration(config.node("poll-interval"));
			if (interval == null) {
				this.plugin.logger().warning("User sync poll interval invalid");
				return;
			}
			this.task = this.plugin.scheduler().runTimer(this, interval);
		}

		if (config.hasChild("log")) {
			this.plugin.logger().warning("The 'user-sync' -> 'log' configuration option is no longer used, please remove it from your config file.");
		}
	}

	@Override
	public void run() {
		final CommentedConfigurationNode config = this.plugin.config().main().node("user-sync");
		Runnable runAfter = null;
		if (config.node("whitelist", "enabled").getBoolean()) {
			runAfter = this::syncWhitelist;
		}

		if (config.node("bans", "enabled").getBoolean()) {
			syncBans(runAfter);
		} else if (runAfter != null) {
			runAfter.run();
		}
	}

	private @Nullable Set<UUID> getUuids(final @NonNull Consumer<@NonNull FilteredUserListBuilder> builderConfigurator) {
		final CommentedConfigurationNode config = this.plugin.config().main().node("user-sync");
		final AbstractLogger logger = this.plugin.logger();

		final List<NamelessUser> users;
		try {
			final NamelessAPI api = this.plugin.apiProvider().api();
			if (api == null) {
				logger.warning("Skipped sync, it looks like the API is not working properly.");
				return null;
			}

			final FilteredUserListBuilder builder = api.users();
			builder.withFilter(UserFilter.INTEGRATION, StandardIntegrationTypes.MINECRAFT);
			builderConfigurator.accept(builder);
			users = builder.makeRequest();
		} catch (final NamelessException e) {
			logger.warning("An error occurred while getting a list of registered users from the website for the bans sync feature.");
			logger.logException(e);
			return null;
		}

		final Set<UUID> uuids = new HashSet<>();
		try {
			final Set<String> excludes = new HashSet<>(config.node("exclude").getList(String.class));
			for (final NamelessUser user : users) {
				try {
					final UUID uuid = user.minecraftUuid();
					if (uuid == null) {
						throw new IllegalStateException("User does not have UUID even though we specifically requested users with Minecraft integration");
					}
					if (!excludes.contains(uuid.toString())) {
						uuids.add(uuid);
					} else {
						logger.fine("Ignoring user " + uuid);
					}
				} catch (final NamelessException e) {
					throw new IllegalStateException("Getting a user uuid should never fail with a network error, it is cached from the listUsers response", e);
				}
			}
		} catch (SerializationException e) {
			logger.warning("Ignoring invalid excludes");
		}
		return uuids;
	}

	private void syncBans(final @Nullable Runnable onComplete) {
		final AbstractLogger logger = this.plugin.logger();
		logger.fine("Starting bans sync, retrieving list of banned users...");
		this.plugin.scheduler().runAsync(() -> {
			final Set<UUID> bannedUuids = getUuids(b -> b.withFilter(UserFilter.BANNED, true));
			if (bannedUuids == null) {
				return;
			}
			this.plugin.scheduler().runSync(() -> {
				final Set<OfflinePlayer> banned = Bukkit.getBannedPlayers();
				for (final UUID bannedUuid : bannedUuids) {
					final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(bannedUuid);
					if (!banned.contains(bannedPlayer)) {
						banned.add(bannedPlayer);
						logger.fine("Added " + bannedUuid + " to the ban list");
						if (bannedPlayer.isOnline()) {
							this.bukkitPlugin.kickPlayer((Player) bannedPlayer, USER_SYNC_KICK);
						}
					}
				}
				logger.fine("Retrieving list of unbanned players...");
				this.plugin.scheduler().runAsync(() -> {
					final Set<UUID> unbannedUuids = getUuids(b -> b.withFilter(UserFilter.BANNED, false));
					if (unbannedUuids == null) {
						return;
					}
					this.plugin.scheduler().runSync(() -> {
						final Set<OfflinePlayer> banned2 = Bukkit.getBannedPlayers();
						for (UUID unbannedUuid : unbannedUuids) {
							final OfflinePlayer unbannedPlayer = Bukkit.getOfflinePlayer(unbannedUuid);
							if (banned2.contains(unbannedPlayer)) {
								banned2.remove(unbannedPlayer);
								logger.fine("Removed " + unbannedUuid + " from the ban list");
							}
						}
						if (onComplete != null) {
							onComplete.run();
						}
					});
				});
			});
		});
	}

	private void syncWhitelist() {
		final CommentedConfigurationNode config = this.plugin.config().main().node("user-sync", "whitelist");
		final AbstractLogger logger = this.plugin.logger();

		final boolean verifiedOnly = config.node("verified-only").getBoolean();
		final int groupIdOnly = config.node("only-with-group").getInt();

		logger.fine("Starting auto-whitelist, retrieving list of registered users...");

		this.plugin.scheduler().runAsync(() -> {
			final Set<UUID> websiteUuids = getUuids(b -> {
				b.withFilter(UserFilter.BANNED, false);
				if (verifiedOnly) {
					b.withFilter(UserFilter.VERIFIED, true);
				}
				if (groupIdOnly >= 0) {
					b.withFilter(UserFilter.GROUP_ID, groupIdOnly);
				}
			});

			if (websiteUuids == null) {
				return;
			}

			this.plugin.scheduler().runSync(() -> {
				logger.fine("Done, updating bukkit whitelist...");

				// Whitelist players who are not whitelisted but should be
				for (final UUID websiteUuid : websiteUuids) {
					final OfflinePlayer player = Bukkit.getOfflinePlayer(websiteUuid);
					if (!player.isWhitelisted()) {
						player.setWhitelisted(true);
						logger.fine("Added " + (player.getName() == null ? websiteUuid.toString() : player.getName()) + " to the whitelist.");
					}
				}

				logger.fine("Done, now retrieving a list of all users to un-whitelist users who shouldn't be whitelisted...");

				this.plugin.scheduler().runAsync(() -> {
					final Set<UUID> allUuids = getUuids(b -> {});

					if (allUuids == null) {
						return;
					}
					allUuids.removeAll(websiteUuids);

					this.plugin.scheduler().runSync(() -> {
						for (final UUID toRemove : allUuids) {
							final OfflinePlayer player = Bukkit.getOfflinePlayer(toRemove);
							if (player.isWhitelisted()) {
								player.setWhitelisted(false);
								logger.fine("Removed " + (player.getName() == null ? toRemove.toString() : player.getName()) + " from the whitelist");
								if (player.isOnline()) {
									this.bukkitPlugin.kickPlayer((Player) player, USER_SYNC_KICK);
								}
							}
						}
					});
				});
			});
		});
	}

}
