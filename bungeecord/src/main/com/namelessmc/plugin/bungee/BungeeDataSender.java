package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

public class BungeeDataSender extends AbstractDataSender {

	public BungeeDataSender(final @NotNull NamelessPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void registerCustomProviders() {
		this.registerPlayerInfoProvider((json, player) -> {
			final ProxiedPlayer bungeePlayer = ProxyServer.getInstance().getPlayer(player.uuid());
			if (bungeePlayer == null) {
				return;
			}
			json.addProperty("address", bungeePlayer.getSocketAddress().toString());
		});
	}

}
