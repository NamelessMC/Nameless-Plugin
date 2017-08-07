package com.namelessmc.plugin.NamelessSpigot;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public enum Permission {
	COMMAND_MAIN("namelessmc.main"),
	COMMAND_REGISTER("namelessmc.register"),
	COMMAND_GETNOTIFICATIONS("namelessmc.notifications"),
	COMMAND_REPORT("namelessmc.report"),
	COMMAND_ADMIN_SETGROUP("namelessmc.admin.setgroup"),
	COMMAND_ADMIN_GETUSER("namelessmc.admin.getuser"),
	ADMIN_UPDATENOTIFY("namelessmc.admin.updatenotify");
	
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
	
	public boolean hasPermission(Player player) {
		if(player.hasPermission(asPermission()))return true;
		return false;
	}
	
	public boolean hasPermission(CommandSender sender) {
		if(sender.hasPermission(asPermission()))return true;
		return false;
	}
	
	public static org.bukkit.permissions.Permission toGroupSyncPermission(String permission) {
		return new org.bukkit.permissions.Permission(permission, PermissionDefault.FALSE);
	}

}
