package com.namelessmc.plugin.oldbukkit.audiences;

import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.bukkit.entity.Player;

public class OldBukkitNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public OldBukkitNamelessPlayer(Player player) {
		super(new LegacyCommandSenderAudience(player), player.getUniqueId(), player.getName());
		this.player = player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return this.player.hasPermission(permission.toString());
	}
}
