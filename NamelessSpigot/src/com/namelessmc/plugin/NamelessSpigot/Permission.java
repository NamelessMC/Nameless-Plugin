package com.namelessmc.plugin.NamelessSpigot;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public enum Permission {

	COMMAND_GET_NOTIFICATIONS("namelessmc.command.getnotifications"),
	COMMAND_REGISTER("namelessmc.command.register"),
	COMMAND_REPORT("namelessmc.command.report"),
	COMMAND_SET_GROUP("namelessmc.command.setgroup"),
	COMMAND_USER_INFO("namelessmc.command.userinfo"),
	COMMAND_VALIDATE("namelessmc.command.validate"),

	COMMAND_NAMELESS("namelessmc.command.nameless"),

	;

	private org.bukkit.permissions.Permission permission;
	private String permissionString;

	Permission(final String permissionString){
		this.permission = new org.bukkit.permissions.Permission(permissionString);
		this.permissionString = permissionString;
	}

	@Override
	public String toString() {
		return this.permissionString;
	}

	public org.bukkit.permissions.Permission asPermission() {
		return this.permission;
	}

	public boolean hasPermission(final CommandSender sender) {
		return sender.hasPermission(this.asPermission());
	}

	public static org.bukkit.permissions.Permission toGroupSyncPermission(final String permission) {
		return new org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE);
	}

}
