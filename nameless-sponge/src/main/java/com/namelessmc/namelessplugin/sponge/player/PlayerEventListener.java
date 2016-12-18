package com.namelessmc.namelessplugin.sponge.player;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import com.namelessmc.namelessplugin.sponge.NamelessPlugin;
import com.namelessmc.namelessplugin.sponge.utils.RequestUtil;

/*
 *  Bungeecord Version by IsS127
 */

public class PlayerEventListener {

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
	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event){
		Player player = event.getTargetEntity();

		if(plugin.getConfig().getNode("join-notifications").getBoolean()){
			RequestUtil request = new RequestUtil(plugin);
			try {
				request.getNotifications(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		plugin.loginCheck(player);
	}

}