package com.namelessmc.plugin.common;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NamelessCommandSender implements Audience {

	private final @NonNull Audience audience;

	public NamelessCommandSender(final @NonNull Audience audience) {
		this.audience = audience;
	}

	@Override
	public void sendMessage(@NonNull final Identity source,
							@NonNull final Component message,
							@NonNull final MessageType type) {
		this.audience.sendMessage(source, message, type);
	}

}
