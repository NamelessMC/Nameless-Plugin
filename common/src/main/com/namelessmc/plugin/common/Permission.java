package com.namelessmc.plugin.common;

import org.jetbrains.annotations.NotNull;

public enum Permission {

	COMMAND_GET_NOTIFICATIONS("namelessmc.command.getnotifications"),
	COMMAND_REGISTER("namelessmc.command.register"),
	COMMAND_REPORT("namelessmc.command.report"),
	COMMAND_SET_GROUP("namelessmc.command.setgroup"),
	COMMAND_USER_INFO("namelessmc.command.userinfo"),
	COMMAND_VERIFY("namelessmc.command.verify"),

	COMMAND_PLUGIN("namelessmc.command.nameless"),

	;

	private final @NotNull String permissionString;

	Permission(final @NotNull String permissionString){
		this.permissionString = permissionString;
	}

	@Override
	public @NotNull String toString() {
		return this.permissionString;
	}

}
