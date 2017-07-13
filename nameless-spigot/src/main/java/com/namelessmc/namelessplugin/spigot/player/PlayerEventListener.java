package com.namelessmc.namelessplugin.spigot.player;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.UpdateChecker;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerNotifications;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerSetGroup;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerUpdateUsername;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessMessages;

public class PlayerEventListener implements Listener {

	NamelessPlugin plugin;

	/*
	 * NamelessConfigs Files
	 */
	YamlConfiguration config;
	YamlConfiguration playerDataConfig;
	YamlConfiguration permissionConfig;

	/*
	 * Constructer
	 */
	public PlayerEventListener(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
	}

	/*
	 * User File check, Name Check, Get notification, Group sync.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {

			@Override
			public void run() {
				updateChecker(player);
				if (plugin.hasSetUrl()) {
					NamelessAPI api = plugin.getAPI();
					NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
					userFileCheck(player);
					if (namelessPlayer.exists()) {
						if (namelessPlayer.isValidated()) {
							userNameCheck(player);
							userGetNotification(player);
							userGroupSync(player);
						} else {
							player.sendMessage(NamelessChat
									.convertColors(NamelessChat.getMessage(NamelessMessages.PLAYER_NOT_VALID)));
						}
					}
				}
			}
		});
	}

	public void updateChecker(Player player) {
		if (player.hasPermission(NamelessPlugin.permissionAdmin + ".updatenotify")) {
			UpdateChecker updateChecker = new UpdateChecker(plugin);
			if (updateChecker.updateNeeded()) {
				updateChecker.sendUpdateMessage(player);
			}
		}
	}

	/*
	 * User Notifications.
	 */
	public void userGetNotification(Player player) {
		config = plugin.getAPI().getConfigManager().getConfig();
		if (config.getBoolean("join-notifications")) {
			NamelessAPI api = plugin.getAPI();
			NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
			NamelessPlayerNotifications notifications = namelessPlayer.getNotifications();
			Integer pms = notifications.getPMs();
			Integer alerts = notifications.getAlerts();
			String errorMessage = notifications.getErrorMessage();
			boolean hasError = notifications.hasError();

			String pmMessage = NamelessChat.getMessage(NamelessMessages.PM_NOTIFICATIONS_MESSAGE).replaceAll("%pms%",
					pms.toString());
			String alertMessage = NamelessChat.getMessage(NamelessMessages.ALERTS_NOTIFICATIONS_MESSAGE)
					.replaceAll("%alerts%", alerts.toString());
			String noNotifications = NamelessChat.getMessage(NamelessMessages.NO_NOTIFICATIONS);

			if (hasError) {
				// Error with request
				player.sendMessage(ChatColor.RED + "Error: " + errorMessage);
			} else if (alerts.equals(0) && pms.equals(0)) {
				player.sendMessage(NamelessChat.convertColors(noNotifications));
			} else if (alerts.equals(0)) {
				player.sendMessage(NamelessChat.convertColors(pmMessage));
			} else if (pms.equals(0)) {
				player.sendMessage(NamelessChat.convertColors(alertMessage));
			} else {
				player.sendMessage(NamelessChat.convertColors(alertMessage));
				player.sendMessage(NamelessChat.convertColors(pmMessage));
			}
		}
	}

	/*
	 * User Group Synchronization.
	 */
	public void userGroupSync(Player player) {
		config = plugin.getAPI().getConfigManager().getConfig();
		if (config.getBoolean("group-synchronization")) {
			permissionConfig = plugin.getAPI().getConfigManager().getGroupSyncPermissionsConfig();
			ConfigurationSection section = permissionConfig.getConfigurationSection("permissions");
			try {
				for (String groupID : section.getKeys(true)) {
					NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
					if (player.hasPermission(section.getString(groupID))) {
						if (namelessPlayer.getGroupID().toString().equals(groupID)) {
							return;
						} else {
							Integer previousgroup = namelessPlayer.getGroupID();
							namelessPlayer.setGroupID(groupID);

							NamelessPlayerSetGroup group = namelessPlayer.setGroupID(groupID);
							if (group.hasError()) {
								NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, "&4Error trying to change &c"
										+ player.getName() + "'s group: &4" + group.getErrorMessage());
							} else if (group.hasSucceeded()) {
								NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO,
										"&aSuccessfully changed &b" + player.getName() + "'s &agroup from &b"
												+ previousgroup + " &ato &b" + group.getNewGroup() + "&a!");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Check if the user exists in the Players Information File.
	 */

	public void userFileCheck(Player player) {

		playerDataConfig = plugin.getAPI().getConfigManager().getPlayerDataConfig();

		if (!playerDataConfig.contains(player.getUniqueId().toString())) {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
					"&c" + player.getName() + " &4is not contained in the Players Data File..");
			playerDataConfig.set(player.getUniqueId().toString() + ".Username", player.getName());
			playerDataConfig.options().copyDefaults(true);

			try {
				playerDataConfig.save(new File(plugin.getDataFolder(), "PlayersData.yml"));
				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO,
						"&aAdded &b" + player.getName() + " &ato the Players Data File.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Update Username on Login.
	 */
	public void userNameCheck(Player player) {

		config = plugin.getAPI().getConfigManager().getConfig();
		playerDataConfig = plugin.getAPI().getConfigManager().getPlayerDataConfig();

		// Check if user has changed Username
		// If so, change the username in the Players Information File.
		// And change the username on the website.
		if (config.getBoolean("update-username")) {
			if (!playerDataConfig.getString(player.getUniqueId().toString() + ".Username").equals(player.getName())
					&& playerDataConfig.contains(player.getUniqueId().toString())) {
				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO,

						"&aDetected that &b" + player.getName() + " &ahas changed his/her username!");
				String previousUsername = playerDataConfig.get(player.getUniqueId().toString() + ".Username")
						.toString();
				String newUsername = player.getName();
				playerDataConfig.set(player.getUniqueId().toString() + ".PreviousUsername", previousUsername);
				playerDataConfig.set(player.getUniqueId().toString() + ".Username", newUsername);
				playerDataConfig.options().copyDefaults(true);

				try {
					playerDataConfig.save(new File(plugin.getDataFolder(), "PlayersData.yml"));
				} catch (IOException e) {
					e.printStackTrace();
				}

				NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
				// Changing username on Website here.
				if (!namelessPlayer.getUserName().equals(newUsername)) {
					NamelessPlayerUpdateUsername updateUsername = namelessPlayer.updateUsername(newUsername);
					if (updateUsername.hasError()) {
						NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,

								"Failed changing &c" + player.getName() + "'s &4username in the website");
					} else {
						NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO,
								"&aChanged &b" + player.getName() + "'s &ausername in the website");
					}
				}
			}
		}
	}

}