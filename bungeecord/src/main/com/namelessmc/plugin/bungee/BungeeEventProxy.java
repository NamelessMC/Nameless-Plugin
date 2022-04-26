package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.namelessmc.plugin.common.event.ServerQuitEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class BungeeEventProxy implements Listener {

	private final @NonNull NamelessPlugin plugin;

	BungeeEventProxy(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(final @NonNull PostLoginEvent event) {
		final NamelessPlayer player = plugin.audiences().player(event.getPlayer().getUniqueId());
		if (player == null) {
			this.plugin.logger().severe("Skipped join event for player " + event.getPlayer().getName() +
					", Audience is null");
			return;
		}
		final ServerJoinEvent event2 = new ServerJoinEvent(player);
		plugin.events().post(event2);
	}

	@EventHandler
	public void onQuit(final @NonNull ServerDisconnectEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		final ServerQuitEvent event2 = new ServerQuitEvent(uuid);
		plugin.events().post(event2);
	}

}
