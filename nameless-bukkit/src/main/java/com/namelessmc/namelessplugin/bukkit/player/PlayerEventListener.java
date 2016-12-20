package com.namelessmc.namelessplugin.bukkit.player;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.namelessmc.namelessplugin.bukkit.NamelessPlugin;
import com.namelessmc.namelessplugin.bukkit.utils.PermissionHandler;
import com.namelessmc.namelessplugin.bukkit.utils.RequestUtil;

public class PlayerEventListener implements Listener {

	NamelessPlugin plugin;

	/*
	 *  Constructer
	 */
	public PlayerEventListener(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
	}

	/*
	 *  Update site username and group on player join
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		RequestUtil request = new RequestUtil(plugin);
		PermissionHandler phandler = new PermissionHandler(plugin);

		if(plugin.getConfig().getBoolean("join-notifications")){
			try {
				request.getNotifications(player.getUniqueId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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

		if(!plugin.getAPIUrl().isEmpty()){
			plugin.userCheck(player);
		}
	}

}