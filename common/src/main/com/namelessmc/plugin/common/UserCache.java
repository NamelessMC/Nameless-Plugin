package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserCache implements Reloadable {

	private final NamelessPlugin plugin;

	private @Nullable AbstractScheduledTask task;
	private List<String> usernames = Collections.emptyList();
	private List<String> minecraftUsernames = Collections.emptyList();

	UserCache(final NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void unload() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
		this.usernames = Collections.emptyList();
	}

	@Override
	public void load() {
		task = this.plugin.scheduler().runTimer(this::update, Duration.ofMinutes(5));
	}

	private void update() {
		this.plugin.scheduler().runAsync(() -> {
			this.plugin.logger().fine("Refreshing user cache");
			final NamelessAPI api = this.plugin.apiProvider().api();
			if (api == null) {
				return;
			}

			try {
				final List<NamelessUser> users = api.users().makeRequest();

				final List<String> usernames = new ArrayList<>(users.size());
				final List<String> minecraftUsernames = new ArrayList<>(users.size());

				for (NamelessUser user : users) {
					usernames.add(user.username());
					minecraftUsernames.add(user.minecraftUsername());
				}

				this.usernames = usernames;
				this.minecraftUsernames = minecraftUsernames;
			} catch (NamelessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private List<String> search(Collection<String> original, String part) {
		return original.stream().filter(s -> s.startsWith(part)).collect(Collectors.toUnmodifiableList());
	}

	public List<String> usernames() {
		return this.usernames;
	}

	public List<String> usernamesSearch(String part) {
		return this.search(this.usernames, part);
	}

	public List<String> minecraftUsernames() {
		return this.minecraftUsernames;
	}

	public List<String> minecraftUsernamesSearch(String part) {
		return this.search(this.minecraftUsernames, part);
	}

}
