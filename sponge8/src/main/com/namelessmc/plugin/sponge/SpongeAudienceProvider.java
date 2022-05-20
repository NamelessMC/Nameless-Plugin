package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongeAudienceProvider extends AbstractAudienceProvider {

	SpongeAudienceProvider() {}

	static void dispatchCommand(String command) {
		try {
			Sponge.server().commandManager().process(command);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public @NotNull NamelessConsole console() {
		return new NamelessConsole(Sponge.systemSubject(), SpongeAudienceProvider::dispatchCommand);
	}

	@Override
	public @NotNull Audience broadcast() {
		return Sponge.server().broadcastAudience();
	}

	private @Nullable NamelessPlayer spongeToNamelessPlayer(final Optional<ServerPlayer> optionalPlayer) {
		if (optionalPlayer.isPresent()) {
			final ServerPlayer player = optionalPlayer.get();
			return new NamelessPlayer(player, player.uniqueId(), player.name());
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
				.map(p -> new NamelessPlayer(p, p.uniqueId(), p.name()))
				.collect(Collectors.toUnmodifiableList());
	}
}
