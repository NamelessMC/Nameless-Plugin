package com.namelessmc.plugin.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;

public abstract class AbstractPermissions
		implements AbstractDataSender.InfoProvider, AbstractDataSender.PlayerInfoProvider {

	public abstract Collection<String> getGroups();

	public abstract Collection<String> getPlayerGroups(final @NonNull NamelessPlayer player);

	private @NonNull JsonArray toJsonArray(final @NonNull Collection<String> coll) {
		final JsonArray json = new JsonArray(coll.size());
		coll.stream().map(JsonPrimitive::new).forEach(json::add);
		return json;
	}

	@Override
	public void addInfoToJson(@NonNull JsonObject json) {
		json.add("groups", this.toJsonArray(this.getGroups()));
	}

	@Override
	public void addInfoToJson(@NonNull JsonObject json, @NonNull NamelessPlayer player) {
		json.add("groups", this.toJsonArray(this.getPlayerGroups(player)));
	}

}
