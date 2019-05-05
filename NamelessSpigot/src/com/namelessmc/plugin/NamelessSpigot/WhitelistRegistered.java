package com.namelessmc.plugin.NamelessSpigot;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.namelessmc.NamelessAPI.NamelessException;

public class WhitelistRegistered implements Runnable {

	public void start() {
		// Return if this feature is disabled
		if (!Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.enabled", false)) {
			return;
		}
		
		final long pollInterval = Config.MAIN.getConfig().getInt("auto-whitelist-registered.poll-interval") * 20;
		Bukkit.getScheduler().runTaskLater(NamelessPlugin.getInstance(), this, pollInterval);
	}
	
	@Override
	public void run() {
		final boolean hideInactive = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.exclude-inactive");
		final boolean hideBanned = Config.MAIN.getConfig().getBoolean("auto-whitelist-registered.exclude-banned");
		
		final Logger logger = NamelessPlugin.getInstance().getLogger();
		
		try {
			final Set<UUID> uuids = NamelessPlugin.getInstance().api.getRegisteredUsers(hideInactive, hideBanned).keySet();
			
			//debug
			System.out.println("[DEBUG] Registered players as received from the website:");
			for (UUID uuid : uuids) {
				System.out.println(uuid + " : " + Bukkit.getOfflinePlayer(uuid).getName());
			}
			
			for (final OfflinePlayer whitelistedPlayer : Bukkit.getWhitelistedPlayers()) {
				if (!uuids.contains(whitelistedPlayer.getUniqueId())) {
					// The player is whitelisted, but no(t) (longer) registered.
					whitelistedPlayer.setWhitelisted(false);
					uuids.remove(whitelistedPlayer.getUniqueId());
					logger.info("Removed " + whitelistedPlayer.getName() + " from the whitelist.");
				}
			}
			
			// All remaining UUIDs in the set are from players that are not on the whitelist yet
			for (final UUID uuid : uuids) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				player.setWhitelisted(true);
				logger.info("Added " + player.getName() + " to the whitelist.");
			}
		} catch (final NamelessException e) {
			logger.warning("An error occured while getting a list of registered users from the website for the auto-whitelist-registered feature.");
			e.printStackTrace();
		}
		
		// Run again after wait time
		final long pollInterval = Config.MAIN.getConfig().getInt("auto-whitelist-registered.poll-interval") * 20;
		Bukkit.getScheduler().runTaskLater(NamelessPlugin.getInstance(), this, pollInterval);
	}

}
