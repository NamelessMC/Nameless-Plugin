package com.namelessmc.plugin.NamelessBungee;

import net.md_5.bungee.api.CommandSender;

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
	
	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(asPermission());
	}

}
