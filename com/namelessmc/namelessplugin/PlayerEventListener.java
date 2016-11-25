package com.namelessmc.namelessplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


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
	public void onPlayerJoin(PlayerJoinEvent e){
		Player player = e.getPlayer();
		
		plugin.loginCheck(player);
	}
}
