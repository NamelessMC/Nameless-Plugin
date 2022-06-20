package com.namelessmc.plugin.oldbukkit;

import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class OldBukkitAudienceProvider extends AbstractAudienceProvider {

	private void dispatchCommand(final @NonNull String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

	@Override
	public @NonNull NamelessConsole console() {
		return new NamelessConsole(new LegacyCommandSenderAudience(Bukkit.getConsoleSender()), this::dispatchCommand);
	}

	@Override
	public Audience broadcast() {
		final Player[] bukkitPlayers = Bukkit.getOnlinePlayers();
		final List<Audience> audiences = new ArrayList<>(bukkitPlayers.length + 1);
		for (Player player : bukkitPlayers) {
			audiences.add(new LegacyCommandSenderAudience(player));
		}
		audiences.add(new LegacyCommandSenderAudience(Bukkit.getConsoleSender()));
		return Audience.audience(audiences);
	}

	public @Nullable NamelessPlayer bukkitToNamelessPlayer(final Player bukkitPlayer) {
		return bukkitPlayer == null
				? null
				: new NamelessPlayer(new LegacyCommandSenderAudience(bukkitPlayer), bukkitPlayer.getUniqueId(), bukkitPlayer.getName());
	}

	@Override
	public @Nullable NamelessPlayer player(@NonNull UUID uuid) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getUniqueId().equals(uuid)) {
				return bukkitToNamelessPlayer(player);
			}
		}
		return null;
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(String username) {
		return bukkitToNamelessPlayer(Bukkit.getPlayerExact(username));
	}

	@Override
	public @NonNull Collection<@NonNull NamelessPlayer> onlinePlayers() {
		return Arrays.stream(Bukkit.getOnlinePlayers())
				.map(p -> new NamelessPlayer(new LegacyCommandSenderAudience(p), p.getUniqueId(), p.getName()))
				.collect(Collectors.toUnmodifiableList());
	}
}
