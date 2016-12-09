package com.namelessmc.namelessplugin.bungeecord.player;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
         
      /*
       * Bungeecord version made by IsS127
       */

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
	public void onPlayerJoin(PostLoginEvent e){
		ProxiedPlayer player = e.getPlayer();
		
		plugin.loginCheck(player);
	}
}
