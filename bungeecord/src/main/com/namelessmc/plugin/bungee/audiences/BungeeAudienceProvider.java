package com.namelessmc.plugin.bungee.audiences;

import com.namelessmc.plugin.bungee.BungeeNamelessPlugin;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeAudienceProvider extends AbstractAudienceProvider {

	private final @NonNull BungeeAudiences audiences;

	public BungeeAudienceProvider(final @NonNull BungeeNamelessPlugin bungeePlugin) {
		this.audiences = BungeeAudiences.create(bungeePlugin);
	}

	@Override
	public @NonNull NamelessConsole console() {
		return new BungeeNamelessConsole(audiences);
	}

	@Override
	public @NonNull Audience broadcast() {
		return audiences.all();
	}

	public @Nullable NamelessPlayer bungeeToNamelessPlayer(final ProxiedPlayer bungeePlayer) {
		return bungeePlayer == null
				? null
				: new BungeeNamelessPlayer(this.audiences, bungeePlayer);
	}

	@Override
	public @Nullable NamelessPlayer player(@NonNull UUID uuid) {
		return bungeeToNamelessPlayer(ProxyServer.getInstance().getPlayer(uuid));
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(@NonNull String username) {
		return bungeeToNamelessPlayer(ProxyServer.getInstance().getPlayer(username));
	}

	@Override
	public @NonNull Collection<@NonNull NamelessPlayer> onlinePlayers() {
		return ProxyServer.getInstance().getPlayers().stream()
				.map(p -> (NamelessPlayer) new BungeeNamelessPlayer(this.audiences, 	p))
				.collect(Collectors.toUnmodifiableList());
	}
}
