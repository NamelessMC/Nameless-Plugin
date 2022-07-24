package com.namelessmc.plugin.bungee.audiences;

import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class BungeeNamelessPlayer extends NamelessPlayer {

	private final ProxiedPlayer player;

	public BungeeNamelessPlayer(BungeeAudiences audiences, ProxiedPlayer player) {
		super(audiences.player(player), player.getUniqueId(), player.getName());
		this.player = player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return this.player.hasPermission(permission.toString());
	}

}
