package com.namelessmc.plugin.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserCache implements Reloadable {

	private final NamelessPlugin plugin;

	private @Nullable AbstractScheduledTask task;
	private List<String> usernames = Collections.emptyList();

	UserCache(final NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		if (task != null) {
			task.cancel();
		}

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
				JsonObject response = api.users().makeRawRequest();
				final JsonArray users = response.getAsJsonArray("users");
				final List<String> usernames = new ArrayList<>(users.size());
				for (final JsonElement userElement : users) {
					final JsonObject user = userElement.getAsJsonObject();
					final String username = user.get("username").getAsString();
					usernames.add(username);
					this.usernames = usernames;
				}
			} catch (NamelessException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public List<String> getUsernames() {
		return this.usernames;
	}

}
