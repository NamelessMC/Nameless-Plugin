package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.NamelessConsole;
import com.namelessmc.plugin.common.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeAudienceProvider extends AbstractAudienceProvider {

	private final @NotNull BungeeAudiences audiences;

	BungeeAudienceProvider(final @NotNull NamelessPluginBungee bungeePlugin) {
		this.audiences = BungeeAudiences.create(bungeePlugin);
	}


	@Override
	public @NotNull NamelessConsole console() {
		return new NamelessConsole(audiences.console());
	}

	@Override
	public @NotNull Audience broadcast() {
		return audiences.all();
	}

	@Override
	public @Nullable NamelessPlayer player(@NotNull UUID uuid) {
		final ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
		if (player == null) {
			return null;
		}
		return new NamelessPlayer(this.audiences.player(player), player.getUniqueId(), player.getName());
	}

	@Override
	public @NotNull Collection<@NotNull NamelessPlayer> onlinePlayers() {
		return ProxyServer.getInstance().getPlayers().stream()
				.map(p -> new NamelessPlayer(this.audiences.player(p), p.getUniqueId(), p.getName()))
				.collect(Collectors.toUnmodifiableList());
	}
}
