package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.NamelessConsole;
import com.namelessmc.plugin.common.NamelessPlayer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class VelocityAudienceProvider extends AbstractAudienceProvider {

	private final @NonNull ProxyServer server;

	VelocityAudienceProvider(final @NonNull ProxyServer server) {
		this.server = server;
	}

	@Override
	public @NonNull NamelessConsole console() {
		return new NamelessConsole(server.getConsoleCommandSource());
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
		return new NamelessPlayer(player, player.getUniqueId(), player.getUsername());
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
				.map(p -> new NamelessPlayer(p, p.getUniqueId(), p.getUsername()))
				.collect(Collectors.toUnmodifiableList());
	}

}
