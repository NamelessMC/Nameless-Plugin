package com.namelessmc.plugin.sponge9.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.api.entity.living.player.Player;

public class SpongeNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public SpongeNamelessPlayer(final ConfigurationHandler config,
								final Player player) {
		super(config, player, player.uniqueId(), player.name());
		this.player = player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		throw new NotImplementedException("How to check for permissions in sponge?");
	}

}
