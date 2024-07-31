package com.namelessmc.plugin.oldbukkit.audiences;

import org.bukkit.entity.Player;

import com.namelessmc.plugin.bukkit.audiences.BukkitNamelessPlayer;
import com.namelessmc.plugin.common.ConfigurationHandler;

public class OldBukkitNamelessPlayer extends BukkitNamelessPlayer {

	public OldBukkitNamelessPlayer(final ConfigurationHandler config,
								   final Player player) {
		super(config, new LegacyCommandSenderAudience(player), player);
	}

}
