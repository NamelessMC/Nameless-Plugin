package com.namelessmc.plugin.sponge7.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
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

	private final ConfigurationHandler config;
	private final SpongeAudiences audiences;
	private final Server server;

	public SpongeAudienceProvider(final ConfigurationHandler config,
								  final SpongeAudiences audiences,
								  final Server server) {
		this.config = config;
		this.audiences = audiences;
		this.server = server;
	}

	@Override
	public @NonNull NamelessConsole console() {
		return new SpongeNamelessConsole(this.audiences);
	}

	@Override
	public @NonNull Audience broadcast() {
		return this.audiences.all();
	}

	private @Nullable NamelessPlayer spongeToNamelessPlayer(final Optional<Player> optionalPlayer) {
		if (optionalPlayer.isPresent()) {
			final Player player = optionalPlayer.get();
			return new SpongeNamelessPlayer(this.config, this.audiences, player);
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
				.map(p -> new SpongeNamelessPlayer(this.config, this.audiences, p))
				.collect(Collectors.toUnmodifiableList());
	}
}
