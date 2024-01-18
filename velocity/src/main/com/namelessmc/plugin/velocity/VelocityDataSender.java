package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.velocitypowered.api.proxy.ProxyServer;

public class VelocityDataSender extends AbstractDataSender {

	private final ProxyServer server;
	
	protected VelocityDataSender(final NamelessPlugin plugin, final ProxyServer server) {
		super(plugin);
		this.server = server;
	}

	@Override
	protected void registerCustomProviders() {
		this.registerGlobalInfoProvider(json ->
				json.addProperty("max_players", server.getConfiguration().getShowMaxPlayers()));
	}

}
