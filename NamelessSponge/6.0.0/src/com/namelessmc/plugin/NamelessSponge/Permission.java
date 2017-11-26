package com.namelessmc.plugin.NamelessSponge;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionDescription.Builder;

public enum Permission {
	COMMAND_MAIN("namelessmc.main"),
	COMMAND_REGISTER("namelessmc.register"),
	COMMAND_GETNOTIFICATIONS("namelessmc.notifications"),
	COMMAND_REPORT("namelessmc.report"),
	COMMAND_ADMIN_SETGROUP("namelessmc.admin.setgroup"),
	COMMAND_ADMIN_GETUSER("namelessmc.admin.getuser"),
	ADMIN_UPDATENOTIFY("namelessmc.admin.updatenotify");
	
	private String permissionString;
	
	Permission(String permissionString){
		this.permissionString = permissionString;
	}
	
	@Override
	public String toString() {
		return permissionString;
	}
	
	public boolean hasPermission(User user) {
		return user.hasPermission(permissionString);
	}
	
	public static String toGroupSyncPermission(String permission) {
		Optional<Builder> optBuilder = NamelessPlugin.permissionsservice.newDescriptionBuilder(NamelessPlugin.getInstance());
		if (optBuilder.isPresent()) {
		    Builder builder = optBuilder.get();
		    builder.id(permission)
		           .assign(PermissionDescription.ROLE_ADMIN, false).assign(PermissionDescription.ROLE_STAFF, false).assign(PermissionDescription.ROLE_USER, false)
		           .register();
		}
		return permission;
	}

}
