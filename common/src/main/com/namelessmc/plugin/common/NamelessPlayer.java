package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class NamelessPlayer extends NamelessCommandSender {

	private final @NonNull UUID uuid;
	private final @NonNull String username;

	public NamelessPlayer(final @NonNull Audience audience,
						  final @NonNull UUID uuid,
						  final @NonNull String username) {
		super(audience);
		this.uuid = uuid;
		this.username = username;
	}

	public @NonNull UUID uuid() {
		return this.uuid;
	}
	
	public @NonNull String websiteUuid() {
		return NamelessAPI.javaUuidToWebsiteUuid(this.uuid);
	}

	public @NonNull String username() {
		return this.username;
	}

}
