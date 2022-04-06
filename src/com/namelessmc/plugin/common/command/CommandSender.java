package com.namelessmc.plugin.common.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class CommandSender implements Audience {

	private final @NotNull Audience audience;

	public CommandSender(final @NotNull Audience audience) {
		this.audience = audience;
	}

	public abstract boolean isPlayer();

	public abstract UUID getUniqueId();

	public abstract String getName();

	@Override
	public void sendMessage(@NotNull final Identity source,
							@NotNull final Component message,
							@NotNull final MessageType type) {
		this.audience.sendMessage(source, message, type);
	}

}
