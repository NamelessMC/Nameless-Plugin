package com.namelessmc.plugin.NamelessBungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public enum Permission {
	COMMAND_MAIN("namelessmc.main"),
	COMMAND_REGISTER("namelessmc.register"),
	COMMAND_GETNOTIFICATIONS("namelessmc.notifications"),
	COMMAND_REPORT("namelessmc.report"),
	COMMAND_ADMIN_SETGROUP("namelessmc.admin.setgroup"),
	COMMAND_ADMIN_GETUSER("namelessmc.admin.getuser"),
	ADMIN_UPDATENOTIFY("namelessmc.admin.updatenotify");
	
	private String permission;
	
	Permission(String permission){
		this.permission = permission;
	}
	
	@Override
	public String toString() {
		return permission;
	}
	
	public String asPermission() {
		return permission;
	}
	
	public boolean hasPermission(ProxiedPlayer player) {
		if(player.hasPermission(asPermission()))return true;
		return false;
	}
	
	public boolean hasPermission(CommandSender sender) {
		if(sender.hasPermission(asPermission()))return true;
		return false;
	}

}
