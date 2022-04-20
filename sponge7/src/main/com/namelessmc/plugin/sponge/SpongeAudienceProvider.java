package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.NamelessConsole;
import com.namelessmc.plugin.common.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongeAudienceProvider extends AbstractAudienceProvider {

	private final @NonNull SpongeAudiences audiences;
	private final @NonNull Server server;

	SpongeAudienceProvider(final @NonNull SpongeAudiences audiences,
						   final @NonNull Server server) {
		this.audiences = audiences;
		this.server = server;
	}

	@Override
	public @NonNull NamelessConsole console() {
		return new NamelessConsole(this.audiences.console());
	}

	@Override
	public @NonNull Audience broadcast() {
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
	public @Nullable NamelessPlayer player(@NonNull UUID uuid) {
		return spongeToNamelessPlayer(this.server.getPlayer(uuid));
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(@NonNull String username) {
		return spongeToNamelessPlayer(this.server.getPlayer(username));
	}

	@Override
	public @NonNull Collection<@NonNull NamelessPlayer> onlinePlayers() {
		return this.server.getOnlinePlayers().stream()
				.map(p -> new NamelessPlayer(this.audiences.player(p), p.getUniqueId(), p.getName()))
				.collect(Collectors.toUnmodifiableList());
	}
}