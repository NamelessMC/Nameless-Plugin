package com.namelessmc.plugin.common;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum Permission {

	COMMAND_GET_NOTIFICATIONS("namelessmc.command.getnotifications"),
	COMMAND_REGISTER("namelessmc.command.register"),
	COMMAND_REPORT("namelessmc.command.report"),
	COMMAND_SET_GROUP("namelessmc.command.setgroup"),
	COMMAND_USER_INFO("namelessmc.command.userinfo"),
	COMMAND_VERIFY("namelessmc.command.verify"),

	COMMAND_PLUGIN("namelessmc.command.nameless"),

	;

	private final @NonNull String permissionString;

	Permission(final @NonNull String permissionString){
		this.permissionString = permissionString;
	}

	@Override
	public @NonNull String toString() {
		return this.permissionString;
	}

}
