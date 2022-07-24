package com.namelessmc.plugin.common.audiences;

import com.namelessmc.plugin.common.Permission;
import net.kyori.adventure.audience.Audience;

public abstract class NamelessConsole extends NamelessCommandSender {

	public NamelessConsole(final Audience audience) {
		super(audience);
	}

	public abstract void dispatchCommand(String command);

	@Override
	public boolean hasPermission(Permission permission) {
		return true;
	}

}
