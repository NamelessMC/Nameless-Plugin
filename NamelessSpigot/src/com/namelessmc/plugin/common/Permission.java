package com.namelessmc.plugin.common;

public enum Permission {

	COMMAND_GET_NOTIFICATIONS("namelessmc.command.getnotifications"),
	COMMAND_REGISTER("namelessmc.command.register"),
	COMMAND_REPORT("namelessmc.command.report"),
	COMMAND_SET_GROUP("namelessmc.command.setgroup"),
	COMMAND_USER_INFO("namelessmc.command.userinfo"),
	COMMAND_VALIDATE("namelessmc.command.validate"),

	COMMAND_NAMELESS("namelessmc.command.nameless"),

	;

	private String permissionString;

	Permission(final String permissionString){
		this.permissionString = permissionString;
	}

	@Override
	public String toString() {
		return this.permissionString;
	}

}
