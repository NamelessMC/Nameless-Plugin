package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.NamelessConsole;
import com.namelessmc.plugin.common.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongeAudienceProvider extends AbstractAudienceProvider {

	private final @NotNull SpongeAudiences audiences;
	private final @NotNull Server server;

	SpongeAudienceProvider(final @NotNull SpongeAudiences audiences,
						   final @NotNull Server server) {
		this.audiences = audiences;
		this.server = server;
	}

	@Override
	public @NotNull NamelessConsole console() {
		return new NamelessConsole(this.audiences.console());
	}

	@Override
	public @NotNull Audience broadcast() {
		return this.audiences.all();
	}

	private @Nullable NamelessPlayer spongeToNamelessPlayer(final Optional<Player> optionalPlayer) {
		if (optionalPlayer.isPresent()) {
			final Player player = optionalPlayer.get();
			return new NamelessPlayer(this.audiences.player(player), player.getUniqueId(), player.getName());
		}
		return null;
	}

	@Override
	public @Nullable NamelessPlayer player(@NotNull UUID uuid) {
		return spongeToNamelessPlayer(this.server.getPlayer(uuid));
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(@NotNull String username) {
		return spongeToNamelessPlayer(this.server.getPlayer(username));
	}

	@Override
	public @NotNull Collection<@NotNull NamelessPlayer> onlinePlayers() {
		return this.server.getOnlinePlayers().stream()
				.map(p -> new NamelessPlayer(this.audiences.player(p), p.getUniqueId(), p.getName()))
				.collect(Collectors.toUnmodifiableList());
	}
}
