package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.NamelessConsole;
import com.namelessmc.plugin.common.NamelessPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpigotAudienceProvider extends AbstractAudienceProvider {

	private final @NotNull BukkitAudiences audiences;

	SpigotAudienceProvider(final @NotNull NamelessPluginSpigot spigotPlugin) {
		this.audiences = BukkitAudiences.create(spigotPlugin);
	}

	@Override
	public @NotNull NamelessConsole console() {
		return new NamelessConsole(audiences.console());
	}

	@Override
	public @NotNull Audience broadcast() {
		return audiences.all();
	}

	public @Nullable NamelessPlayer bukkitToNamelessPlayer(final Player bukkitPlayer) {
		return bukkitPlayer == null
				? null
				: new NamelessPlayer(this.audiences.player(bukkitPlayer), bukkitPlayer.getUniqueId(), bukkitPlayer.getName());
	}

	@Override
	public @Nullable NamelessPlayer player(@NotNull UUID uuid) {
		return bukkitToNamelessPlayer(Bukkit.getPlayer(uuid));
	}

	@Override
	public @Nullable NamelessPlayer playerByUsername(@NotNull String username) {
		return bukkitToNamelessPlayer(Bukkit.getPlayerExact(username));
	}

	@Override
	public @NotNull Collection<@NotNull NamelessPlayer> onlinePlayers() {
		return Bukkit.getOnlinePlayers().stream()
				.map(p -> new NamelessPlayer(this.audiences.player(p), p.getUniqueId(), p.getName()))
				.collect(Collectors.toUnmodifiableList());
	}
}
