package com.namelessmc.plugin.NamelessSponge.event;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import com.namelessmc.plugin.NamelessSponge.NamelessPlugin;

public class PlayerQuit {
	
	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect event) {
		Player player = event.getTargetEntity();
		NamelessPlugin.LOGIN_TIME.remove(player.getUniqueId());
	}

}
