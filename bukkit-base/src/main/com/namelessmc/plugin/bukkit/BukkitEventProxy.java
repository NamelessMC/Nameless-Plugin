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
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BukkitEventProxy implements Listener {

	private final @NotNull NamelessPlugin plugin;

	BukkitEventProxy(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}


	@EventHandler
	public void onJoin(final @NotNull PlayerJoinEvent event) {
		final Player bukkitPlayer = event.getPlayer();
		final NamelessPlayer player = new NamelessPlayer(
				this.plugin.audiences().player(bukkitPlayer.getUniqueId()),
				bukkitPlayer.getUniqueId(),
				bukkitPlayer.getName());
		final ServerJoinEvent event2 = new ServerJoinEvent(player);
		this.plugin.events().post(event2);
	}

	@EventHandler
	public void onQuit(final @NotNull PlayerQuitEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		final ServerQuitEvent event2 = new ServerQuitEvent(uuid);
		this.plugin.events().post(event2);
	}

}
