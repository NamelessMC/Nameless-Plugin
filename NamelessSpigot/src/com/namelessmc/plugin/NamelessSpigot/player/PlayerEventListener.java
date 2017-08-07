package com.namelessmc.plugin.NamelessSpigot.player;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Config;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;

public class PlayerEventListener implements Listener {

	/*
	 * User File check, Name Check, Get notification, Group sync.
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			if (namelessPlayer.exists()) {
				if (namelessPlayer.isValidated()) {
					// shouldnt be outisde validated cause if your not validated you cant check anything.
					userGetNotifications(player);
				} else {
					player.sendMessage(Message.PLAYER_NOT_VALID.getMessage());
				}
				// theese should be outside, better!
				userNameCheck(player);
				userGroupSync(player);
			}
		});
	}

	/*
	 * public void updateChecker(Player player) { if
	 * (player.hasPermission(NamelessPlugin.PERMISSIONS_ADMIN_UPDATENOTIFY)) {
	 * UpdateChecker updateChecker = new UpdateChecker(plugin); if
	 * (updateChecker.updateNeeded()) {s updateChecker.sendUpdateMessage(player); }
	 * } }
	 */

	/*
	 * User Notifications.
	 */
	public void userGetNotifications(Player player) {
		YamlConfiguration config = Config.MAIN.getConfig();
		if (config.getBoolean("join-notifications")) {
			try {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				int messages = namelessPlayer.getMessageCount();
				int alerts = namelessPlayer.getAlertCount();

				String pmMessage = Message.NOTIFICATIONS_MESSAGES.getMessage().replace("%pms%", messages + "");
				String alertMessage = Message.NOTIFICATIONS_ALERTS.getMessage().replace("%alerts%", alerts + "");
				String noNotifications = Message.NO_NOTIFICATIONS.getMessage();
				if (alerts == 0 && messages == 0) {
					player.sendMessage(noNotifications);
				} else if (alerts == 0) {
					player.sendMessage(pmMessage);
				} else if (messages == 0) {
					player.sendMessage(alertMessage);
				} else {
					player.sendMessage(alertMessage);
					player.sendMessage(pmMessage);
				}
			} catch (NamelessException e) {
				String errorMessage = Message.NOIFICATIONS_ERROR.getMessage().replace("%error%", e.getMessage());
				player.sendMessage(errorMessage);
				e.printStackTrace();
			}
		}
	}

	/*
	 * User Group Synchronization.
	 */
	public void userGroupSync(Player player) {
		YamlConfiguration config = Config.MAIN.getConfig();
		if (config.getBoolean("group-synchronization")) {
			YamlConfiguration permissionConfig = Config.MAIN.getConfig();
			for (String groupID : permissionConfig.getConfigurationSection("permissions").getKeys(true)) {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				if (String.valueOf(namelessPlayer.getGroupID()).equals(groupID)) {
					return;
				} else if (player.hasPermission(Permission.toGroupSyncPermission(permissionConfig.getString("permissions" + groupID)))) {
					Integer previousgroup = namelessPlayer.getGroupID();
					String successPlayerMessage = Message.GROUP_SYNC_PLAYER_ERROR.getMessage();
					try {
						namelessPlayer.setGroup(Integer.parseInt(groupID));
						NamelessPlugin.log(Level.INFO, "&aSuccessfully changed &b" + player.getName() + "'s &agroup from &b"
								+ previousgroup + " &ato &b" + groupID + "&a!");
						player.sendMessage(successPlayerMessage);
					} catch (NumberFormatException e) {
						NamelessPlugin.log(Level.WARNING, "&4The Group ID is not a Integer/Number!");
					} catch (NamelessException e) {
						String errorPlayerMessage = Message.GROUP_SYNC_PLAYER_ERROR.getMessage().replace("%error%", e.getMessage());
						NamelessPlugin.log(Level.WARNING, "&4Error changing &c"
								+ player.getName() + "'s group: &4" + e.getMessage());
						player.sendMessage(errorPlayerMessage);
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void userNameCheck(Player player) {
		YamlConfiguration playerData = Config.PLAYER_INFO.getConfig();

		if (playerData.getBoolean("update-username")) {
			//Save data if not in file
			if (!playerData.contains(player.getUniqueId().toString())) {
				playerData.set(player.getUniqueId().toString() + ".username", player.getName());
				return;
			}
			
			//If the name in the file is different, the player has changed they name
			String previousName = playerData.getString(player.getUniqueId() + ".username");
			if (!previousName.equals(player.getName())) {
				NamelessPlugin.log(Level.INFO, "&cDetected that &a" + player.getName() + " &chas changed his/her username.");
	
				//Update name in file
				playerData.set(player.getUniqueId() + ".Username", player.getName());

				//Now change username on website
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				try {
					namelessPlayer.updateUsername(player.getName());
					String successMessage = Message.USERNAME_SYNC_SUCCESS.getMessage();
					NamelessPlugin.log(Level.INFO, "&Updated &b" + player.getName() + "'s &ausername in the website");
					player.sendMessage(successMessage);
				} catch (NamelessException e) {
					String errorMessage = Message.USERNAME_SYNC_ERROR.getMessage().replace("%error%", e.getMessage());
					NamelessPlugin.log(Level.WARNING,"&4Failed updating &c" + player.getName() + "'s &4username in the website, Error:" + e.getMessage());
					player.sendMessage(errorMessage);
					e.printStackTrace();
				}
			}
		}
	}

}