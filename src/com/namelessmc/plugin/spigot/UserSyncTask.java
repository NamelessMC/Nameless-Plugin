package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.*;
import com.namelessmc.java_api.integrations.DetailedIntegrationData;
import com.namelessmc.java_api.integrations.DetailedMinecraftIntegrationData;
import com.namelessmc.java_api.integrations.StandardIntegrationTypes;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class UserSyncTask implements Runnable {

	@Override
	public void run() {
		final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();
		final boolean doLog = config.getBoolean("user-sync.log", true);
		Runnable runAfter = null;
		if (config.getBoolean("user-sync.whitelist.enabled", false)) {
			runAfter = () -> this.syncWhitelist(doLog);
		}

		if (config.getBoolean("user-sync.bans.enabled", false)) {
			syncBans(runAfter, doLog);
		} else if (runAfter != null) {
			runAfter.run();
		}
	}

	@Nullable
	private Set<UUID> getUuids(final boolean doLog,
							   final @NotNull Consumer<@NotNull FilteredUserListBuilder> builderConfigurator) {
		final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();
		final AbstractLogger logger = NamelessPlugin.getInstance().getCommonLogger();

		List<NamelessUser> users;
		try {
			final Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
			if (optApi.isPresent()) {
				FilteredUserListBuilder builder = optApi.get().getRegisteredUsers();
				builder.withFilter(UserFilter.INTEGRATION, StandardIntegrationTypes.MINECRAFT);
				builderConfigurator.accept(builder);
				users = builder.makeRequest();
			} else {
				logger.warning("Skipped sync, it looks like the API is not working properly.");
				return null;
			}
		} catch (final NamelessException e) {
			logger.warning("An error occured while getting a list of registered users from the website for the bans sync feature.");
			logger.logException(e);
			return null;
		}

		final Set<UUID> uuids = new HashSet<>();
		final Set<String> excludes = new HashSet<>(config.getStringList("user-sync.exclude"));
		for (final NamelessUser user : users) {
			try {
				if (NamelessPlugin.getInstance().getApiProvider().useUsernames()) {
					String name = user.getUsername();
					if (!excludes.contains(name)) {
						UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
						uuids.add(offlineUuid);
					} else if (doLog) {
						logger.info("Ignoring user " + name);
					}
				} else {
					Map<String, DetailedIntegrationData> integrations = user.getIntegrations();
					DetailedIntegrationData minecraftIntegration = integrations.get(StandardIntegrationTypes.MINECRAFT);
					if (minecraftIntegration == null) {
						throw new IllegalStateException("User does not have Minecraft integration even though we specifically requested users with Minecraft integration only");
					}
					final UUID uuid = ((DetailedMinecraftIntegrationData) minecraftIntegration).getUniqueId();
					if (!excludes.contains(uuid.toString())) {
						uuids.add(uuid);
					} else if (doLog) {
						logger.info("Ignoring user " + uuid);
					}
				}
			} catch (final NamelessException e) {
				throw new IllegalStateException("Getting a user uuid should never fail with a network error, it is cached from the listUsers response", e);
			}
		}
		return uuids;
	}

	private void syncBans(final @Nullable Runnable onComplete,
						  final boolean doLog) {
		final AbstractLogger logger = NamelessPlugin.getInstance().getCommonLogger();

		if (doLog) {
			logger.info("Starting bans sync, retrieving list of banned users...");
		}
		Bukkit.getScheduler().runTaskAsynchronously((NamelessPlugin.getInstance()), () -> {
			Set<UUID> bannedUuids = getUuids(doLog, b -> b.withFilter(UserFilter.BANNED, true));
			if (bannedUuids == null) {
				return;
			}
			Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
				Set<OfflinePlayer> banned = Bukkit.getBannedPlayers();
				for (UUID bannedUuid : bannedUuids) {
					OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(bannedUuid);
					if (!banned.contains(bannedPlayer)) {
						banned.add(bannedPlayer);
						if (doLog) {
							logger.info("Added " + bannedUuid + " to the ban list");
						}
						if (bannedPlayer.isOnline()) {
							String message = NamelessPlugin.getInstance().getLanguage()
									.getLegacyMessage(LanguageHandler.Term.USER_SYNC_KICK);
							((Player) bannedPlayer).kickPlayer(message);
						}
					}
				}
				if (doLog) {
					logger.info("Retrieving list of unbanned players...");
				}
				Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
					Set<UUID> unbannedUuids = getUuids(doLog, b -> b.withFilter(UserFilter.BANNED, false));
					if (unbannedUuids == null) {
						return;
					}
					Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
						Set<OfflinePlayer> banned2 = Bukkit.getBannedPlayers();
						for (UUID unbannedUuid : unbannedUuids) {
							OfflinePlayer unbannedPlayer = Bukkit.getOfflinePlayer(unbannedUuid);
							if (banned2.contains(unbannedPlayer)) {
								banned2.remove(unbannedPlayer);
								if (doLog) {
									logger.info("Removed " + unbannedUuid + " from the ban list");
								}
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

	private void syncWhitelist(final boolean doLog) {
		final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();
		final AbstractLogger logger = NamelessPlugin.getInstance().getCommonLogger();

		final boolean verifiedOnly = config.getBoolean("user-sync.whitelist.verified-only");
		final int groupIdOnly = config.getInt("user-sync.whitelist.only-with-group");

		if (doLog) {
			logger.info("Starting auto-whitelist, retrieving list of registered users...");
		}

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final Set<UUID> websiteUuids = getUuids(doLog, b -> {
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

			Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
				if (doLog) {
					logger.info("Done, updating bukkit whitelist...");
				}

				// Whitelist players who are not whitelisted but should be
				for (final UUID websiteUuid : websiteUuids) {
					final OfflinePlayer player = Bukkit.getOfflinePlayer(websiteUuid);
					if (!player.isWhitelisted()) {
						player.setWhitelisted(true);
						if (doLog) {
							logger.info("Added " + (player.getName() == null ? websiteUuid.toString() : player.getName()) + " to the whitelist.");
						}
					}
				}

				if (doLog) {
					logger.info("Done, now retrieving a list of all users to un-whitelist users who shouldn't be whitelisted...");
				}

				Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
					final Set<UUID> allUuids = getUuids(doLog, b -> {});

					if (allUuids == null) {
						return;
					}
					allUuids.removeAll(websiteUuids);

					Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
						for (UUID toRemove : allUuids) {
							OfflinePlayer player = Bukkit.getOfflinePlayer(toRemove);
							if (player.isWhitelisted()) {
								player.setWhitelisted(false);
								if (doLog) {
									logger.info("Removed " + (player.getName() == null ? toRemove.toString() : player.getName()) + " from the whitelist");
								}
								if (player.isOnline()) {
									String message = NamelessPlugin.getInstance().getLanguage()
											.getLegacyMessage(LanguageHandler.Term.USER_SYNC_KICK);
									((Player) player).kickPlayer(message);
								}
							}
						}
					});
				});
			});
		});
	}

}
