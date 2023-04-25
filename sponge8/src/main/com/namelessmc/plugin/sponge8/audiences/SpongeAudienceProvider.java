package com.namelessmc.plugin.sponge8.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongeAudienceProvider extends AbstractAudienceProvider {

	private final ConfigurationHandler config;

	public SpongeAudienceProvider(final ConfigurationHandler config) {
		this.config = config;
	}

	@Override
	public @NotNull NamelessConsole console() {
		return new SpongeNamelessConsole();
	}

	@Override
	public @NotNull Audience broadcast() {
		return Sponge.server().broadcastAudience();
	}

	private @Nullable NamelessPlayer spongeToNamelessPlayer(final Optional<ServerPlayer> optionalPlayer) {
		if (optionalPlayer.isPresent()) {
			final ServerPlayer player = optionalPlayer.get();
			return new SpongeNamelessPlayer(this.config, player);
		}
		return null;
	}

	@Override
	public @Nullable NamelessPlayer player(final @NotNull UUID uuid) {
		return spongeToNamelessPlayer(Sponge.server().player(uuid));
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(final @NotNull String username) {
		return spongeToNamelessPlayer(Sponge.server().player(username));
	}

	@Override
	public @NotNull Collection<@NotNull NamelessPlayer> onlinePlayers() {
		return Sponge.server().onlinePlayers().stream()
				.map(p -> new SpongeNamelessPlayer(this.config, p))
				.collect(Collectors.toUnmodifiableList());
	}
}
