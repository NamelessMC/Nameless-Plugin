package com.namelessmc.namelessplugin.spigot.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
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
	 *  Update site username and group on player join
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();

		if(plugin.getConfig().getBoolean("join-notifications")){
			RequestUtil request = new RequestUtil(plugin);
			try {
				request.getNotifications(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(!plugin.getAPIUrl().isEmpty()){
			plugin.userCheck(player);
		}
	}

}