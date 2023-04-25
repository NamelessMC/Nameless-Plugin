package com.namelessmc.plugin.velocity.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class VelocityAudienceProvider extends AbstractAudienceProvider {

	private final ConfigurationHandler config;
	private final ProxyServer server;

	public VelocityAudienceProvider(final ConfigurationHandler config,
									final ProxyServer server) {
		this.config = config;
		this.server = server;
	}

	@Override
	public @NonNull NamelessConsole console() {
		return new VelocityNamelessConsole(this.server);
	}

	@Override
	public @NonNull Audience broadcast() {
		final Collection<Player> players = server.getAllPlayers();
		final List<Audience> everyone = new ArrayList<>(players.size() + 1);
		everyone.addAll(players);
		everyone.add(server.getConsoleCommandSource());
		return Audience.audience(everyone);
	}

	private @Nullable NamelessPlayer velocityToNamelessPlayer(final Optional<Player> optionalPlayer) {
		if (optionalPlayer.isEmpty()) {
			return null;
		}

		Player player = optionalPlayer.get();
		return new VelocityNamelessPlayer(this.config, player);
	}

	@Override
	public @Nullable NamelessPlayer player(final @NonNull UUID uuid) {
		return velocityToNamelessPlayer(server.getPlayer(uuid));
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(@NonNull String username) {
		return velocityToNamelessPlayer(server.getPlayer(username));
	}

	@Override
	public @NonNull Collection<@NonNull NamelessPlayer> onlinePlayers() {
		return server.getAllPlayers().stream()
				.map(p -> new VelocityNamelessPlayer(this.config, p))
				.collect(Collectors.toUnmodifiableList());
	}

}
