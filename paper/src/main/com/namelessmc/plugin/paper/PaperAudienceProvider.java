package com.namelessmc.plugin.paper;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.NamelessConsole;
import com.namelessmc.plugin.common.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaperAudienceProvider extends AbstractAudienceProvider {

	@Override
	public @NonNull NamelessConsole console() {
		return new NamelessConsole(Bukkit.getConsoleSender());
	}

	@Override
	public @NonNull Audience broadcast() {
		final Collection<? extends Player> bukkitPlayers = Bukkit.getOnlinePlayers();
		final List<Audience> audiences = new ArrayList<>(bukkitPlayers.size() + 1);
		audiences.addAll(bukkitPlayers);
		audiences.add(Bukkit.getConsoleSender());
		return Audience.audience(audiences);
	}

	public @Nullable NamelessPlayer bukkitToNamelessPlayer(final Player bukkitPlayer) {
		return bukkitPlayer == null
				? null
				: new NamelessPlayer(bukkitPlayer, bukkitPlayer.getUniqueId(), bukkitPlayer.getName());
	}

	@Override
	public @Nullable NamelessPlayer player(@NonNull UUID uuid) {
		return bukkitToNamelessPlayer(Bukkit.getPlayer(uuid));
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(@NonNull String username) {
		return bukkitToNamelessPlayer(Bukkit.getPlayerExact(username));
	}

	@Override
	public @NonNull Collection<@NonNull NamelessPlayer> onlinePlayers() {
		return Bukkit.getOnlinePlayers().stream()
				.map(p -> new NamelessPlayer(p, p.getUniqueId(), p.getName()))
				.collect(Collectors.toUnmodifiableList());
	}
}
