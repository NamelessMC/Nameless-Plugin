package com.namelessmc.plugin.spigot;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.namelessmc.NamelessAPI.NamelessException;

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
		final boolean hideInactive = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.exclude-inactive");
		final boolean hideBanned = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.exclude-banned");
		final boolean log = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.log");
		
		List<String> permWhitelistUUID = new List<>();
		
		final Logger logger = NamelessPlugin.getInstance().getLogger();

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final Set<UUID> uuids;
			try {
				uuids = NamelessPlugin.getInstance().api.getRegisteredUsers(hideInactive, hideBanned).keySet();
			} catch (final NamelessException e) {
				logger.warning(
						"An error occured while getting a list of registered users from the website for the auto-whitelist-registered feature.");
				e.printStackTrace();
				return;
			}
			
			Config.MAIN.getConfig().getStringList("permanent-whitelisted")
			.forEach(playername -> permWhitelistUUID.add(playername.getUniqueId()));		

			Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
				for (final OfflinePlayer whitelistedPlayer : Bukkit.getWhitelistedPlayers()) {
					if (!uuids.contains(whitelistedPlayer.getUniqueId())) {
						// The player is not on the permanent whitelist exclusion list
						if (!permanentWhitelisted.contains(whitelistedPlayer.getUniqueId())) {						
							// The player is whitelisted, but no(t) (longer) registered.
							whitelistedPlayer.setWhitelisted(false);
							uuids.remove(whitelistedPlayer.getUniqueId());
							if (log) {
								logger.info("Removed " + whitelistedPlayer.getName() + " from the whitelist.");
							}
						}
					}
				}

				// All remaining UUIDs in the set are from players that are not on the whitelist yet
				for (final UUID uuid : uuids) {
					final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
					player.setWhitelisted(true);
					if (log) {
						logger.info("Added " + player.getName() + " to the whitelist.");
					}
				}

				// Run again after wait time
				final long pollInterval = Config.MAIN.getConfig().getInt("auto-whitelist-registered.poll-interval") * 20;
				Bukkit.getScheduler().runTaskLater(NamelessPlugin.getInstance(), this, pollInterval);
			});
		});
	}

}
