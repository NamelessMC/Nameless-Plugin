package com.namelessmc.plugin.common;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class NamelessCommandSender implements Audience {

	private final @NotNull Audience audience;

	public NamelessCommandSender(final @NotNull Audience audience) {
		this.audience = audience;
	}

	@Override
	public void sendMessage(@NotNull final Identity source,
							@NotNull final Component message,
							@NotNull final MessageType type) {
		this.audience.sendMessage(source, message, type);
	}

}
