package com.namelessmc.plugin.sponge9.audiences;

import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;

public class SpongeNamelessPlayer extends NamelessPlayer {

	private final Player player;

	public SpongeNamelessPlayer(final ConfigurationHandler config,
								final Player player) {
		super(config, player, player.uniqueId(), player.name());
		this.player = player;
	}

	@Override
	public boolean hasPermission(final Permission permission) {
		try {
			final User user = Sponge.server().userManager().load(this.player.uniqueId()).get(100, TimeUnit.MILLISECONDS).orElseThrow();
			return user.hasPermission(permission.toString());
		} catch (InterruptedException | ExecutionException | TimeoutException | NoSuchElementException e) {
			e.printStackTrace();
			return false;
		}
	}

}
