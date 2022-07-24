package com.namelessmc.plugin.velocity.audiences;

import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public class VelocityNamelessConsole extends NamelessConsole implements ConsoleCommandSource {

	private final ProxyServer server;

	public VelocityNamelessConsole(ProxyServer server) {
		super(server.getConsoleCommandSource());
		this.server = server;
	}

	@Override
	public void dispatchCommand(String command) {
		this.server.getCommandManager().executeAsync(this, command);
	}

	@Override
	public Tristate getPermissionValue(String permission) {
		return Tristate.TRUE;
	}

}
