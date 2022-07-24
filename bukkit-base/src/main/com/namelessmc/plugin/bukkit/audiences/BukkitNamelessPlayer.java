package com.namelessmc.plugin.bukkit.audiences;

import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

public class BukkitNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public BukkitNamelessPlayer(Audience audience, Player player) {
		super(audience, player.getUniqueId(), player.getName());
		this.player = player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return this.player.hasPermission(permission.toString());
	}

}
