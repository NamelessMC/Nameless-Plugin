package com.namelessmc.plugin.spigot;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.UserFilter;

public class WhitelistRegistered implements Runnable {

	public WhitelistRegistered() {
		// Return if this feature is disabled
		if (!Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.enabled", false)) {
			return;
		}

		this.run();
	}

	@Override
	public void run() {
		final boolean verified = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.verified");
		final boolean log = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.log");
		final Logger logger = NamelessPlugin.getInstance().getLogger();

		if (log) {
			logger.info("Starting auto-whitelist");
		}

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			UserFilter<?>[] filters;
			if (verified) {
				filters = new UserFilter<?>[] {UserFilter.VERIFIED, UserFilter.UNBANNED};
			} else {
				filters = new UserFilter<?>[] {UserFilter.UNBANNED};
			}

			if (log) {
				logger.info("Retrieving list of users...");
			}

			List<NamelessUser> users;
			try {
				Optional<NamelessAPI> optApi = NamelessPlugin.getInstance().getNamelessApi();
				if (optApi.isPresent()) {
					users = optApi.get().getRegisteredUsers(filters);
				} else {
					logger.warning("Skipped getting list of registered users, it looks like the API is not working properly.");
					return;
				}
			} catch (final NamelessException e) {
				logger.warning(
						"An error occured while getting a list of registered users from the website for the auto-whitelist-registered feature.");
				e.printStackTrace();
				return;
			}

			final Set<UUID> uuids = new HashSet<>();
			for (final NamelessUser user : users) {
				try {
					final Optional<UUID> optUuid = user.getUniqueId();
					if (optUuid.isPresent()) {
						uuids.add(optUuid.get());
					} else {
						logger.warning("Website user " + user.getUsername() + " does not have a UUID!");
					}
				} catch (final NamelessException e) {
					logger.warning("A user has been skipped due to a website communication error");
				}
			}

			if (log) {
				logger.info("Done, updating bukkit whitelist...");
			}

			final Set<String> excludes = new HashSet<>();
			excludes.addAll(Config.MAIN.getConfig().getStringList("auto-whitelist-registered.log"));

			Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
				// Remove players who aren't supposed to be whitelisted
				for (final OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {
					final UUID uuid = player.getUniqueId();
					if (!excludes.contains(uuid.toString()) && !uuids.contains(uuid)) {
						if (log) {
							logger.info("Removed " + player.getName() == null ? uuid.toString() : player.getName() + " from the whitelist.");
						}
						player.setWhitelisted(false);
					}
				}

				// Whitelist players who are not whitelisted but should be
				for (final UUID uuid : uuids) {
					final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
					if (player.isWhitelisted()) {
						continue;
					}
					player.setWhitelisted(true);
					if (log) {
						logger.info("Added " + player.getName() == null ? uuid.toString() : player.getName() + " to the whitelist.");
					}
				}

				// Run again after wait time
				final long pollInterval = Config.MAIN.getConfig().getInt("auto-whitelist-registered.poll-interval") * 20;
				Bukkit.getScheduler().runTaskLater(NamelessPlugin.getInstance(), this, pollInterval);
			});
		});
	}

}
