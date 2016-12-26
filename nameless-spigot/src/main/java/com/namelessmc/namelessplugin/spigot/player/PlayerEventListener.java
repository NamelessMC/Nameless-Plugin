package com.namelessmc.namelessplugin.spigot.player;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.utils.PermissionHandler;
import com.namelessmc.namelessplugin.spigot.utils.RequestUtil;

public class PlayerEventListener implements Listener {

	NamelessPlugin plugin;

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
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		if(!plugin.getAPIUrl().isEmpty()){
			userFileCheck(player);
			userNameCheck(player);
			userGetNotification(player);
			userGroupSync(player);
		
	   }
	}

	/*
	 * User Notifications.
	 */
	public void userGetNotification(Player player){
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
	public void userGroupSync(Player player){
		RequestUtil request = new RequestUtil(plugin);
		PermissionHandler phandler = new PermissionHandler(plugin);
		if(plugin.getConfig().getBoolean("group-synchronization")){
			ConfigurationSection permissions = phandler.getConfig().getConfigurationSection("permissions");
			try {
				for(String groupId : permissions.getKeys(false)){
					if(request.getGroup(player.getName()).equals(groupId)){
						return;
					} else if(player.hasPermission(permissions.getString(groupId))){
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
	
	public void userFileCheck(Player player){
		// Check if user does NOT contain information in the Players Information file. 
		// If so, add him.
		File iFile = new File(plugin.getDataFolder() + File.separator + "playersInformation.yml");
		YamlConfiguration yFile;
		yFile = YamlConfiguration.loadConfiguration(iFile);
		if(!yFile.contains(player.getUniqueId().toString())){
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&a" + player.getName() + " &cis not contained in the Player Information File.."));
			plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Adding &a" + player.getName() + " &2to the Player Information File."));
			yFile.addDefault(player.getUniqueId().toString() + ".Username", player.getName());
			yFile.options().copyDefaults(true);
			try {
				yFile.save(iFile);
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Added &a" + player.getName() + " &2to the Player Information File."));
			} catch (IOException e) {
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&cCould not add &a" + player.getName() + " &cto the Player Information File!"));
				e.printStackTrace();
			}
		}
	}
	
	
	/*
	 *  Update Username on Login.
	 */
	public void userNameCheck(Player player){
		File iFile = new File(plugin.getDataFolder() + File.separator + "playersInformation.yml");
		YamlConfiguration yFile;
		yFile = YamlConfiguration.loadConfiguration(iFile);
		
		// Check if user has changed Username
		// If so, change the username in the Players Information File. (NOT COMPLETED)
		// And change the username on the website.
		if(plugin.getConfig().getBoolean("update-username")){
			if(!yFile.getString(player.getUniqueId() + ".Username").equals(player.getName()) && yFile.contains(player.getUniqueId().toString())){
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&cDetected that&a" + player.getName() + " &chas changed his/her username!"));
				plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changing &a" + player.getName() + "s &2username."));

				String previousUsername = yFile.get(player.getUniqueId() + ".Username").toString();
				String newUsername = player.getName();
				yFile.addDefault(player.getUniqueId() + ".PreviousUsername", previousUsername);
				yFile.set(player.getUniqueId() + ".Username", newUsername);
				yFile.options().copyDefaults(true);
				try {
					yFile.save(iFile);
					plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&2Changed &a" + player.getName() + "s &2username in the Player Information File."));
				} catch (IOException e) {
					plugin.getLogger().info(ChatColor.translateAlternateColorCodes('&',"&c Could not change &a" + player.getName() + "s &2Username in the Player Information File."));
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