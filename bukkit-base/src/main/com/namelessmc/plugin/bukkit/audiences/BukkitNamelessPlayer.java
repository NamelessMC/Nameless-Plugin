package com.namelessmc.plugin.bukkit.audiences;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;

import net.kyori.adventure.audience.Audience;

public class BukkitNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public BukkitNamelessPlayer(final ConfigurationHandler config, final Audience audience, final Player player) {
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

	@Override
	public boolean isVanished() {
		// Supposedly works for at least SuperVanish and PremiumVanish
		// https://github.com/NamelessMC/Nameless-Plugin/issues/412#issuecomment-2567065505
		for (MetadataValue meta : player.getMetadata("vanished")) {
			if (meta.asBoolean()) {
				return true;
			}
		}
		return false;
	}

}
