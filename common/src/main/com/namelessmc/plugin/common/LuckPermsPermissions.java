package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class LuckPermsPermissions extends AbstractPermissions {

	private @Nullable LuckPerms api;

	@Override
	public void reload() {
		try {
			this.api = LuckPermsProvider.get();
		} catch (IllegalStateException e) {}
	}

	@Override
	public boolean isUsable() {
		return this.api != null;
	}

	@Override
	public Set<String> getGroups() {
		if (this.api == null) {
			throw new ProviderNotUsableException();
		}
		return this.api.getGroupManager().getLoadedGroups().stream()
				.map(Group::getName)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Set<String> getPlayerGroups(@NonNull NamelessPlayer player) {
		if (this.api == null) {
			throw new ProviderNotUsableException();
		}
		final User user = this.api.getUserManager().getUser(player.uuid());
		if (user == null) {
			return null;
		}
		return user.getInheritedGroups(QueryOptions.defaultContextualOptions()).stream()
				.map(Group::getName)
				.collect(Collectors.toUnmodifiableSet());
	}
}
