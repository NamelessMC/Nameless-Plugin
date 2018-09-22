package com.namelessmc.plugin.NamelessSpigot;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public enum Permission {

	//COMMAND_REGISTER("namelessmc.register"),
	//COMMAND_GETNOTIFICATIONS("namelessmc.notifications"),
	//COMMAND_REPORT("namelessmc.report"),
	//COMMAND_ADMIN_SETGROUP("namelessmc.admin.setgroup"),
	//COMMAND_ADMIN_GETUSER("namelessmc.admin.getuser"),
	//COMMAND_VALIDATE("namelessmc.validate"),
	//ADMIN_UPDATENOTIFY("namelessmc.admin.updatenotify");
	
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
	
	Permission(String permissionString){
		permission = new org.bukkit.permissions.Permission(permissionString);
		this.permissionString = permissionString;
	}
	
	@Override
	public String toString() {
		return permissionString;
	}
	
	public org.bukkit.permissions.Permission asPermission() {
		return permission;
	}
	
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(asPermission());
	}
	
	public static org.bukkit.permissions.Permission toGroupSyncPermission(String permission) {
		return new org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE);
	}

}
