package com.namelessmc.plugin.paper;

import com.namelessmc.plugin.bukkit.audiences.BukkitNamelessConsole;
import com.namelessmc.plugin.bukkit.audiences.BukkitNamelessPlayer;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PaperAudienceProvider extends AbstractAudienceProvider {

	private void dispatchCommand(final @NonNull String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	@Override
	public @NonNull NamelessConsole console() {
		return new BukkitNamelessConsole(Bukkit.getConsoleSender());
	}

	@Override
	public @NonNull Audience broadcast() {
		final Collection<? extends Player> bukkitPlayers = Bukkit.getOnlinePlayers();
		final List<Audience> audiences = new ArrayList<>(bukkitPlayers.size() + 1);
		audiences.addAll(bukkitPlayers);
		audiences.add(Bukkit.getConsoleSender());
		return Audience.audience(audiences);
	}

	@SuppressWarnings("nullness") // Checker framework thinks bukkitPlayer.getName() is nullable
	public @Nullable NamelessPlayer bukkitToNamelessPlayer(final @Nullable Player bukkitPlayer) {
		return bukkitPlayer == null
				? null
				: new BukkitNamelessPlayer(bukkitPlayer, bukkitPlayer);
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
				.map(p -> (NamelessPlayer) new BukkitNamelessPlayer(p, p))
				.toList();
	}
}
