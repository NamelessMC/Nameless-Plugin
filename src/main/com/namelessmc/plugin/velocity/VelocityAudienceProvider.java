package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.NamelessConsole;
import com.namelessmc.plugin.common.NamelessPlayer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class VelocityAudienceProvider extends AbstractAudienceProvider {

	private final @NotNull ProxyServer server;

	VelocityAudienceProvider(final @NotNull ProxyServer server) {
		this.server = server;
	}

	@Override
	public @NotNull NamelessConsole console() {
		return new NamelessConsole(server.getConsoleCommandSource());
	}

	@Override
	public @NotNull Audience broadcast() {
		List<Audience> everyone = new ArrayList<>();
		everyone.addAll(server.getAllPlayers());
		everyone.add(server.getConsoleCommandSource());
		return Audience.audience(everyone);
	}

	@Override
	public @Nullable NamelessPlayer player(final @NotNull UUID uuid) {
		final Optional<Player> optionalPlayer = server.getPlayer(uuid);
		if (optionalPlayer.isEmpty()) {
			return null;
		}

		Player player = optionalPlayer.get();
		return new NamelessPlayer(player, player.getUniqueId(), player.getUsername());
	}

	@Override
	public @NotNull Collection<@NotNull NamelessPlayer> onlinePlayers() {
		return server.getAllPlayers().stream()
				.map(p -> new NamelessPlayer(p, p.getUniqueId(), p.getUsername()))
				.collect(Collectors.toUnmodifiableList());
	}

}
