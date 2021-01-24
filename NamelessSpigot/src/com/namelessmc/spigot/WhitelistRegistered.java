package com.namelessmc.spigot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			UserFilter<?>[] filters;
			if (verified) {
				filters = new UserFilter<?>[] {UserFilter.VERIFIED, UserFilter.UNBANNED};
			} else {
				filters = new UserFilter<?>[] {UserFilter.UNBANNED};
			}
			
			List<NamelessUser> users;
			try {
				users = NamelessPlugin.getApi().getRegisteredUsers(filters);
			} catch (final NamelessException e) {
				logger.warning(
						"An error occured while getting a list of registered users from the website for the auto-whitelist-registered feature.");
				e.printStackTrace();
				return;
			}
			
			final Set<UUID> uuids = users.stream()
					.map(user -> {
						try {
							return user.getUniqueId();
						} catch (final NamelessException e) {
							return null;
						}
					})
					.filter(opt -> opt != null && opt.isPresent())
					.map(opt -> opt.get())
					.collect(Collectors.toSet());
			
			// Use Set for O(1) contains()
			final Set<String> excludes = new HashSet<>();
			excludes.addAll(Config.MAIN.getConfig().getStringList("auto-whitelist-registered.log"));

			Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
				for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
					final UUID uuid = player.getUniqueId();
					final String name = player.getName() == null ? uuid.toString() : player.getName();
					if (excludes.contains(uuid.toString())) {
						continue;
					}
					
					if (uuids.contains(uuid) && !player.isWhitelisted()) {
						if (log) {
							logger.info("Added " + name + " to the whitelist.");
						}
						player.setWhitelisted(true);
					} else if (!uuids.contains(uuid) && player.isWhitelisted()) {
						if (log) {
							logger.info("Removed " + name + " from the whitelist.");
						}
						player.setWhitelisted(false);
					}
				}

				// All remaining UUIDs in the set are from players that are not on the whitelist yet
				for (final UUID uuid : uuids) {
					final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
					player.setWhitelisted(true);
					if (log) {
						final String name = player.getName() == null ? uuid.toString() : player.getName();
						logger.info("Added " + name + " to the whitelist.");
					}
				}

				// Run again after wait time
				final long pollInterval = Config.MAIN.getConfig().getInt("auto-whitelist-registered.poll-interval") * 20;
				Bukkit.getScheduler().runTaskLater(NamelessPlugin.getInstance(), this, pollInterval);
			});
		});
	}

}
