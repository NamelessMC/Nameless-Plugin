package com.namelessmc.plugin.sponge7.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.spongepowered.api.entity.living.player.Player;

public class SpongeNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public SpongeNamelessPlayer(final ConfigurationHandler config,
								final SpongeAudiences audiences,
								final Player player) {
		super(config, audiences.player(player), player.getUniqueId(), player.getName());
		this.player = player;
	}

	@Override
	public boolean hasPermission(Permission permission) {
		return this.player.hasPermission(permission.toString());
	}

}
