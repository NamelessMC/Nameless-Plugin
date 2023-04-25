package com.namelessmc.plugin.bukkit.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

public class BukkitNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public BukkitNamelessPlayer(final ConfigurationHandler config,
								final Audience audience,
								final Player player) {
		super(config, audience, player.getUniqueId(), player.getName());
		this.player = player;
	}

	public Player bukkitPlayer() {
		return this.player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return this.player.hasPermission(permission.toString());
	}

}
