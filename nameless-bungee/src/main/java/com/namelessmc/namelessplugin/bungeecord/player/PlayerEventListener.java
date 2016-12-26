package com.namelessmc.namelessplugin.bungeecord.player;

import java.io.File;
import java.io.IOException;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.utils.ConfigUtil;
import com.namelessmc.namelessplugin.bungeecord.utils.PermissionHandler;
import com.namelessmc.namelessplugin.bungeecord.utils.RequestUtil;

import net.md_5.bungee.api.ChatColor;
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

public class PlayerEventListener implements Listener {

	NamelessPlugin plugin;

	/*
	 * Config Files
	 */
	Configuration config;
	Configuration playerInfoFile;
	
	/*
	 *  Constructer
	 */
	public PlayerEventListener(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
	}

	/*
	 *  User File check, Name Check, Get notification, Group sync.
	 */
	@EventHandler
	public void onPlayerJoin(PostLoginEvent event){
		ProxiedPlayer player = event.getPlayer();
		
		if(plugin.hasSetUrl){
			userFileCheck(player);
			userNameCheck(player);
			userGetNotification(player);
			userGroupSync(player);
		
	   }
	}

	/*
	 * User Notifications.
	 */
	public void userGetNotification(ProxiedPlayer player){
		RequestUtil request = new RequestUtil(plugin);
		if(plugin.getConfig().getBoolean("join-notifications")){
			try {
				request.getNotifications(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * User Group Synchronization.
	 */
	public void userGroupSync(ProxiedPlayer player){
		RequestUtil request = new RequestUtil(plugin);
		PermissionHandler phandler = new PermissionHandler(plugin);
		if(plugin.getConfig().getBoolean("group-synchronization")){
			Configuration section = phandler.getConfig().getSection("permissions");
			try {
				for(String groupId : section.getKeys()){
					if(request.getGroup(player.getName()).equals(groupId)){
						return;
					} else if(player.hasPermission(section.getString(groupId))){
						request.setGroup(player.getName(), groupId);
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
	
	public void userFileCheck(ProxiedPlayer player){
		try {
			playerInfoFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "playersInformation.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		ConfigUtil conf = new ConfigUtil();

		if(!conf.contains(playerInfoFile,player.getUniqueId().toString())){
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&a" + player.getName() + " &cis not contained in the Player Information File.."));
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Adding &a" + player.getName() + " &2to the Player Information File."));
			playerInfoFile.set(player.getUniqueId().toString() + ".Username", player.getName());

			try {
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(playerInfoFile, new File(plugin.getDataFolder(), "playersInformation.yml"));
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Added &a" + player.getName() + " &2to the Player Information File."));	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/*
	 *  Update Username on Login.
	 */
	public void userNameCheck(ProxiedPlayer player){  
		try {
			playerInfoFile = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "playersInformation.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		ConfigUtil conf = new ConfigUtil();

		// Check if user has changed Username
		// If so, change the username in the Players Information File. (NOT COMPLETED)
		// And change the username on the website.
		if(plugin.getConfig().getBoolean("update-username")){
			if(!playerInfoFile.getString(player.getUniqueId() + ".Username").equals( player.getName()) && conf.contains(playerInfoFile,player.getUniqueId().toString())){
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&cDetected that &a" + player.getName() + " &chas changed his/her username!"));
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changing &a" + player.getName() + "s &2username."));

				String previousUsername = playerInfoFile.get(player.getUniqueId() + ".Username").toString();
				String newUsername = player.getName();
				playerInfoFile.set(player.getUniqueId() + ".PreviousUsername", previousUsername);
				playerInfoFile.set(player.getUniqueId() + ".Username", newUsername);
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changed &a" + player.getName() + "s &2username in the Player Information File."));

				try {
					ConfigurationProvider.getProvider(YamlConfiguration.class).save(playerInfoFile, new File(plugin.getDataFolder(), "playersInformation.yml"));
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Changing username on Website here.
				RequestUtil request = new RequestUtil(plugin);
				try {
					if(!player.getName().equals(request.getUserName(player.getUniqueId().toString()))){
						request.updateUserName(player.getUniqueId().toString(), newUsername);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}