package com.namelessmc.namelessplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerEventListener implements Listener {
	/*
	 *  Update site username and group on player join
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		Player player = e.getPlayer();
		
		NamelessPlugin.pluginInstance.loginCheck(player);
	}
}
