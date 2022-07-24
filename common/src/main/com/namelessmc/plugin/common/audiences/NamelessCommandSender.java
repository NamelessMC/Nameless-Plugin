package com.namelessmc.plugin.common.audiences;

import com.namelessmc.plugin.common.Permission;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class NamelessCommandSender implements Audience {

	private final @NonNull Audience audience;

	public NamelessCommandSender(final @NonNull Audience audience) {
		this.audience = audience;
	}

	public abstract boolean hasPermission(Permission permission);

	@Override
	public void sendMessage(final @NonNull Identity source,
							final @NonNull Component message,
							final @NonNull MessageType type) {
		this.audience.sendMessage(source, message, type);
	}

}
