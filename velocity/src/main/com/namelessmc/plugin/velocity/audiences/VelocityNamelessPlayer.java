package com.namelessmc.plugin.velocity.audiences;

import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.velocitypowered.api.proxy.Player;

public class VelocityNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public VelocityNamelessPlayer(Player player) {
		super(player, player.getUniqueId(), player.getUsername());
		this.player = player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return this.player.hasPermission(permission.toString());
	}

}
