package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.event.NamelessJoinEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

public class VelocityEventProxy {

	private final @NonNull NamelessPlugin plugin;

	VelocityEventProxy(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Subscribe
	public void onJoin(final @NonNull ServerConnectedEvent event) {
		final NamelessPlayer player = this.plugin.audiences().player(event.getPlayer().getUniqueId());
		if (player == null) {
			this.plugin.logger().severe("Skipped join event for player " + event.getPlayer().getUsername() +
					", Audience is null");
			return;
		}
		final NamelessJoinEvent event2 = new NamelessJoinEvent(player);
		this.plugin.events().post(event2);
	}

}
