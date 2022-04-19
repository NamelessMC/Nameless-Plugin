package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.namelessmc.plugin.common.event.ServerQuitEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class BungeeEventProxy implements Listener {

	private final @NotNull NamelessPlugin plugin;

	BungeeEventProxy(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(final @NotNull PostLoginEvent event) {
		final NamelessPlayer player = plugin.audiences().player(event.getPlayer().getUniqueId());
		final ServerJoinEvent event2 = new ServerJoinEvent(Objects.requireNonNull(player));
		plugin.events().post(event2);
	}

	@EventHandler
	public void onQuit(final @NotNull ServerDisconnectEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		final ServerQuitEvent event2 = new ServerQuitEvent(uuid);
		plugin.events().post(event2);
	}

}
