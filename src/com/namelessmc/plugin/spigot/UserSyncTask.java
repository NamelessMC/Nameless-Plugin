package com.namelessmc.plugin.spigot;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.UserFilter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UserSyncTask implements Runnable {

	@Override
	public void run() {
		Logger logger = NamelessPlugin.getInstance().getLogger();
		boolean doLog = Config.MAIN.getConfig().getBoolean("user-sync.log", true);
		Runnable runAfter = null;
		if (Config.MAIN.getConfig().getBoolean("user-sync.whitelist.enabled", false)) {
			runAfter = () -> this.syncWhitelist(doLog, logger);
		}

		if (Config.MAIN.getConfig().getBoolean("user-sync.bans.enabled", false)) {
			syncBans(runAfter, doLog, logger);
		} else if (runAfter != null) {
			runAfter.run();
		}
	}

	@Nullable
	private Set<UUID> getUuids(UserFilter... userFilters) {
		List<NamelessUser> users;
		try {
			final Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
			if (optApi.isPresent()) {
				users = optApi.get().getRegisteredUsers(UserFilter.BANNED);
			} else {
				NamelessPlugin.getInstance().getLogger().warning("Skipped ban sync, it looks like the API is not working properly.");
				return null;
			}
		} catch (final NamelessException e) {
			NamelessPlugin.getInstance().getLogger().warning(
					"An error occured while getting a list of registered users from the website for the bans sync feature.");
			e.printStackTrace();
			return null;
		}

		final Set<UUID> uuids = new HashSet<>();
		for (final NamelessUser user : users) {
			try {
				if (NamelessPlugin.getInstance().getApiProvider().useUuids()) {
					final Optional<UUID> optUuid = user.getUniqueId();
					if (optUuid.isPresent()) {
						uuids.add(optUuid.get());
					} else {
						NamelessPlugin.getInstance().getLogger().warning("Website user " + user.getUsername() + " does not have a UUID!");
					}
				} else {
					String name = user.getUsername();
					UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
					uuids.add(offlineUuid);
				}
			} catch (final NamelessException e) {
				throw new IllegalStateException("Getting a user uuid should never fail with a network error, it is cached from the listUsers response", e);
			}
		}
		final Set<UUID> excludes = Config.MAIN.getConfig().getStringList("auto-whitelist-registered.log").stream().map(UUID::fromString).collect(Collectors.toSet());
		uuids.removeIf(excludes::contains);
		return uuids;
	}

	private void syncBans(@Nullable Runnable onComplete, boolean doLog, @NotNull Logger logger) {
		if (doLog) {
			logger.info("Starting bans sync, retrieving list of banned users...");
		}
		Bukkit.getScheduler().runTaskAsynchronously((NamelessPlugin.getInstance()), () -> {
			Set<UUID> bannedUuids = getUuids(UserFilter.BANNED);
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
							((Player) bannedPlayer).kickPlayer("You were banned on the website"); // TODO translation
						}
					}
				}
				if (doLog) {
					logger.info("Retrieving list of unbanned players...");
				}
				Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
					Set<UUID> unbannedUuids = getUuids(UserFilter.UNBANNED);
					if (unbannedUuids == null) {
						return;
					}
					Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
						Set<OfflinePlayer> banned2 = Bukkit.getBannedPlayers();
						for (UUID unbannedUuid : unbannedUuids) {
							OfflinePlayer unbannedPlayer = Bukkit.getOfflinePlayer(unbannedUuid);
							if (banned.contains(unbannedPlayer)) {
								banned.remove(unbannedPlayer);
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

	private void syncWhitelist(boolean doLog, @NotNull Logger logger) {
		final boolean verifiedOnly = Config.MAIN.getConfig().getBoolean("user-sync.whitelist.verified-only");

		if (doLog) {
			logger.info("Starting auto-whitelist");
		}

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			Set<UUID> websiteUuids = verifiedOnly
					? getUuids(UserFilter.VERIFIED, UserFilter.UNBANNED)
					: getUuids(UserFilter.UNBANNED);

			if (websiteUuids == null) {
				return;
			}

			Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
				if (doLog) {
					logger.info("Done, updating bukkit whitelist...");
				}

				// Remove players who aren't supposed to be whitelisted
				for (final OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
					final UUID uuid = player.getUniqueId();
					if (!websiteUuids.contains(uuid)) {
						if (doLog) {
							logger.info("Removed " + (player.getName() == null ? uuid.toString() : player.getName()) + " from the whitelist.");
						}
						player.setWhitelisted(false);
					}
				}

				// Whitelist players who are not whitelisted but should be
				for (final UUID websiteUuid : websiteUuids) {
					final OfflinePlayer player = Bukkit.getOfflinePlayer(websiteUuid);
					if (player.isWhitelisted()) {
						continue;
					}
					player.setWhitelisted(true);
					if (doLog) {
						logger.info("Added " + (player.getName() == null ? websiteUuid.toString() : player.getName()) + " to the whitelist.");
					}
				}
			});
		});
	}

}
