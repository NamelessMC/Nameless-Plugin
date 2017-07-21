package com.namelessmc.namelessplugin.bungeecord.player;

import java.io.File;
import java.io.IOException;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayerNotifications;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessMessages;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

/*
 *  Bungeecord Version by IsS127
 */

@SuppressWarnings("static-access")
public class PlayerEventListener implements Listener {

	NamelessPlugin plugin;
	NamelessChat chat = plugin.getAPI().getChat();

	/*
	 * NamelessConfigs Files
	 */
	Configuration config;
	Configuration playerDataConfig;
	Configuration permissionConfig;

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
	public void onPlayerJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
			@Override
			public void run() {
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
							player.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.PLAYER_NOT_VALID)));
						}
					}
				}
			}
		});
	}

	/*
	 * User Notifications.
	 */
	public void userGetNotification(ProxiedPlayer player) {
		config = plugin.getAPI().getConfigManager().getConfig();
		if (config.getBoolean("join-notifications")) {
			NamelessAPI api = plugin.getAPI();
			NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
			NamelessPlayerNotifications notifications = namelessPlayer.getNotifications();
			Integer pms = notifications.getPMs();
			Integer alerts = notifications.getAlerts();
			String errorMessage = notifications.getErrorMessage();
			boolean hasError = notifications.hasError();

			String pmMessage = chat.getMessage(NamelessMessages.PM_NOTIFICATIONS_MESSAGE)
					.replaceAll("%pms%", pms.toString());
			String alertMessage = chat.getMessage(NamelessMessages.ALERTS_NOTIFICATIONS_MESSAGE)
					.replaceAll("%alerts%", alerts.toString());
			String noNotifications = chat.getMessage(NamelessMessages.NO_NOTIFICATIONS);

			if (hasError) {
				// Error with request
				player.sendMessage(
						TextComponent.fromLegacyText(ChatColor.RED + "Error: " + errorMessage));
			} else if (alerts.equals(0) && pms.equals(0)) {
				player.sendMessage(chat.convertColors(noNotifications));
			} else if (alerts.equals(0)) {
				player.sendMessage(chat.convertColors(pmMessage));
			} else if (pms.equals(0)) {
				player.sendMessage(chat.convertColors(alertMessage));
			} else {
				player.sendMessage(chat.convertColors(alertMessage));
				player.sendMessage(chat.convertColors(pmMessage));
			}
		}
	}

	/*
	 * User Group Synchronization.
	 */
	public void userGroupSync(ProxiedPlayer player) {
		config = plugin.getAPI().getConfigManager().getConfig();
		if (config.getBoolean("group-synchronization")) {
			permissionConfig = plugin.getAPI().getConfigManager().getGroupSyncPermissionsConfig();
			Configuration section = permissionConfig.getSection("permissions");
			try {
				for (String groupID : section.getKeys()) {
					NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
					if (namelessPlayer.getGroupID().toString().equals(groupID)) {
						return;
					} else if (player.hasPermission(section.getString(groupID))) {
						namelessPlayer.setGroupID(groupID);
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

	public void userFileCheck(ProxiedPlayer player) {

		playerDataConfig = plugin.getAPI().getConfigManager().getPlayerDataConfig();

		if (!plugin.getAPI().getConfigManager().contains(playerDataConfig, player.getUniqueId().toString())) {
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',
					"&a" + player.getName() + " &cis not contained in the Players Data File.."));
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',
					"&2Adding &a" + player.getName() + " &2to the Players Data File."));
			playerDataConfig.set(player.getUniqueId().toString() + ".Username", player.getName());

			try {
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(playerDataConfig,
						new File(plugin.getDataFolder(), "PlayersData.yml"));
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',
						"&2Added &a" + player.getName() + " &2to the Players Data File."));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Update Username on Login.
	 */
	public void userNameCheck(ProxiedPlayer player) {

		playerDataConfig = plugin.getAPI().getConfigManager().getPlayerDataConfig();

		// Check if user has changed Username
		// If so, change the username in the Players Information File. (NOT
		// COMPLETED)
		// And change the username on the website.
		if (playerDataConfig.getBoolean("update-username")) {
			if (!playerDataConfig.getString(player.getUniqueId() + ".Username").equals(player.getName())
					&& plugin.getAPI().getConfigManager().contains(playerDataConfig, player.getUniqueId().toString())) {
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',
						"&cDetected that &a" + player.getName() + " &chas changed his/her username!"));
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',
						"&2Changing &a" + player.getName() + "s &2username."));

				String previousUsername = playerDataConfig.get(player.getUniqueId() + ".Username").toString();
				String newUsername = player.getName();
				playerDataConfig.set(player.getUniqueId() + ".PreviousUsername", previousUsername);
				playerDataConfig.set(player.getUniqueId() + ".Username", newUsername);
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',
						"&2Changed &a" + player.getName() + "s &2username in the Player Information File."));

				try {
					ConfigurationProvider.getProvider(YamlConfiguration.class).save(playerDataConfig,
							new File(plugin.getDataFolder(), "PlayersData.yml"));
				} catch (IOException e) {
					e.printStackTrace();
				}

				NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
				// Changing username on Website here.
				if (!namelessPlayer.getUserName().equals(player.getName())) {
					namelessPlayer.updateUsername(newUsername);
				}
			}
		}
	}

}