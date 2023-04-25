package com.namelessmc.plugin.bungee.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeNamelessPlayer extends NamelessPlayer {

	private final ProxiedPlayer player;

	public BungeeNamelessPlayer(final ConfigurationHandler config, final BungeeAudiences audiences, final ProxiedPlayer player) {
		super(config, audiences.player(player), player.getUniqueId(), player.getName());
		this.player = player;
	}

	public ProxiedPlayer bungeePlayer() {
		return this.player;
	}

	@Override
	public boolean hasPermission(final Permission permission) {
		return this.player.hasPermission(permission.toString());
	}

}
