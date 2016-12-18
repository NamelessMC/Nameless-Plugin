package com.namelessmc.namelessplugin.bungeecord.player;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.utils.RequestUtil;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/*
 *  Bungeecord Version by IsS127
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
	public void onPlayerJoin(PostLoginEvent event){
		ProxiedPlayer player = event.getPlayer();
		
		if(plugin.getConfig().getBoolean("join-notifications")){
			RequestUtil request = new RequestUtil(plugin);
			try {
				request.getNotifications(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(plugin.hasSetUrl){
		  plugin.userCheck(player);
		}
	}

}