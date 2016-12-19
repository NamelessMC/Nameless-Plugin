package com.namelessmc.namelessplugin.sponge.player;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import com.namelessmc.namelessplugin.sponge.NamelessPlugin;
import com.namelessmc.namelessplugin.sponge.utils.PermissionHandler;
import com.namelessmc.namelessplugin.sponge.utils.RequestUtil;

import ninja.leaping.configurate.ConfigurationNode;

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
		RequestUtil request = new RequestUtil(plugin);
		PermissionHandler phandler = new PermissionHandler(plugin);

		if(plugin.getConfig().getNode("join-notifications").getBoolean()){
			
			try {
				request.getNotifications(player.getUniqueId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(plugin.getConfig().getNode("group-synchronization").getBoolean()){
			try {
				for(ConfigurationNode cfgGroupId : phandler.getConfig().getNode("permissions").getChildrenList()){
					phandler.getConfig().getNode("permissions" ,cfgGroupId);
					if(request.getGroup(player.getName()).equals(cfgGroupId)){
						return;
					} else if(player.hasPermission(cfgGroupId.getString())){
						String[] groupId = cfgGroupId.getPath().toString().split(".");
						request.setGroup(player.getName(), groupId[1]);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		plugin.loginCheck(player);
	}

}