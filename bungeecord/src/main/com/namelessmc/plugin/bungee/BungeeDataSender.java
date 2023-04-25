package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.bungee.audiences.BungeeNamelessPlayer;
import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

public class BungeeDataSender extends AbstractDataSender {

	public BungeeDataSender(final @NonNull NamelessPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void registerCustomProviders() {
		// Max players
		this.registerGlobalInfoProvider(json ->
				json.addProperty("max_players", ProxyServer.getInstance().getConfig().getPlayerLimit()));

		// Player address
		this.registerPlayerInfoProvider((json, player) -> {
			final ProxiedPlayer bungeePlayer = ((BungeeNamelessPlayer) player).bungeePlayer();
			json.addProperty("address", bungeePlayer.getSocketAddress().toString());
		});
	}

}
