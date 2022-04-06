package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class VelocityAudienceProvider extends AbstractAudienceProvider {

	private final @NotNull ProxyServer server;

	VelocityAudienceProvider(final @NotNull ProxyServer server) {
		this.server = server;
	}

	@Override
	public Audience console() {
		return server.getConsoleCommandSource();
	}

	@Override
	public Audience all() {
		return Audience.audience(server.getAllPlayers());
	}

	@Override
	public Audience player(final @NotNull UUID uuid) {
//		return server.getPlayer(uuid).<Audience>orElse(Audience.empty());

		final Optional<Player> optionalPlayer = server.getPlayer(uuid);
		if (optionalPlayer.isPresent()) {
			return optionalPlayer.get();
		} else {
			return Audience.empty();
		}
	}

}
