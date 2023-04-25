package com.namelessmc.plugin.velocity.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.velocitypowered.api.proxy.Player;

public class VelocityNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public VelocityNamelessPlayer(final ConfigurationHandler config,
								  final Player player) {
		super(config, player, player.getUniqueId(), player.getUsername());
		this.player = player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return this.player.hasPermission(permission.toString());
	}

}
