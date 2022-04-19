package com.namelessmc.plugin.common;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class NamelessPlayer extends NamelessCommandSender {

	private final @NotNull UUID uuid;
	private final @NotNull String username;

	public NamelessPlayer(final @NotNull Audience audience,
						  final @NotNull UUID uuid,
						  final @NotNull String username) {
		super(audience);
		this.uuid = uuid;
		this.username = username;
	}

	public @NotNull UUID getUniqueId() {
		return this.uuid;
	}

	public @NotNull String getUsername() {
		return this.username;
	}

}
