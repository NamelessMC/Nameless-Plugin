package com.namelessmc.plugin.bungee.audiences;

import com.namelessmc.plugin.common.audiences.NamelessConsole;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;

public class BungeeNamelessConsole extends NamelessConsole {

	public BungeeNamelessConsole(BungeeAudiences audiences) {
		super(audiences.console());
	}

	@Override
	public void dispatchCommand(String command) {
		ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
	}

}
