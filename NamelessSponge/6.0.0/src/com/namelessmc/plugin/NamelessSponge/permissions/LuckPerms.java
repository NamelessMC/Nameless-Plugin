package com.namelessmc.plugin.NamelessSponge.permissions;

import java.util.Optional;

import org.spongepowered.api.entity.living.player.User;

import me.lucko.luckperms.api.LuckPermsApi;

public class LuckPerms implements Permissions{

	@Override
	public String getGroup(User user) {
		// returns an empty Optional if the APi is not loaded
		Optional<LuckPermsApi> oapi = me.lucko.luckperms.LuckPerms.getApiSafe();
		if(oapi.isPresent()) {
			LuckPermsApi api = oapi.get();
			return api.getGroup(api.getUser(user.getName()).getPrimaryGroup()).getFriendlyName();
		}else {
			return null;
		}
	}
}
