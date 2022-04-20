package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.namelessmc.plugin.common.event.ServerQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class BukkitEventProxy implements Listener {

	private final @NonNull NamelessPlugin plugin;

	BukkitEventProxy(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onJoin(final @NonNull PlayerJoinEvent event) {
		final Player bukkitPlayer = event.getPlayer();
		final NamelessPlayer player = new NamelessPlayer(
				this.plugin.audiences().player(bukkitPlayer.getUniqueId()),
				bukkitPlayer.getUniqueId(),
				bukkitPlayer.getName());
		final ServerJoinEvent event2 = new ServerJoinEvent(player);
		this.plugin.events().post(event2);
	}

	@EventHandler
	public void onQuit(final @NonNull PlayerQuitEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		final ServerQuitEvent event2 = new ServerQuitEvent(uuid);
		this.plugin.events().post(event2);
	}

}
