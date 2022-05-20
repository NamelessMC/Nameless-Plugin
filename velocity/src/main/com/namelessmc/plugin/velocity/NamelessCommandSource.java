package com.namelessmc.plugin.velocity;

import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;

public class NamelessCommandSource implements ConsoleCommandSource {

	private static final NamelessCommandSource INSTANCE = new NamelessCommandSource();

	public static NamelessCommandSource instance() {
		return INSTANCE;
	}

	@Override
	public Tristate getPermissionValue(String permission) {
		return Tristate.TRUE;
	}

}
