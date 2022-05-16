package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import com.namelessmc.plugin.common.event.ServerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class SpongeEventProxy {

	private final @NonNull NamelessPlugin plugin;

	SpongeEventProxy(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Listener
	public void onJoin(ClientConnectionEvent.Join event) {
		final NamelessPlayer player = plugin.audiences().player(event.getTargetEntity().getUniqueId());
		if (player == null) {
			this.plugin.logger().severe("Skipped join event for player " + event.getTargetEntity().getName() +
					", Audience is null");
			return;
		}
		final ServerJoinEvent event2 = new ServerJoinEvent(player);
		this.plugin.events().post(event2);
	}

	@Listener
	public void onQuit(ClientConnectionEvent.Disconnect event) {
		final ServerQuitEvent event2 = new ServerQuitEvent(event.getTargetEntity().getUniqueId());
		this.plugin.events().post(event2);
	}

}
