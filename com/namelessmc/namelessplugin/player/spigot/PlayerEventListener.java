package com.namelessmc.namelessplugin.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.namelessmc.namelessplugin.NamelessPlugin;


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
