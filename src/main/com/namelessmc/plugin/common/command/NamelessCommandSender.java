package com.namelessmc.plugin.common.command;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class NamelessCommandSender implements Audience {

	private final @Nullable UUID uuid;
	private final @Nullable String name;
	private final @NotNull Audience audience;

	public NamelessCommandSender(final @NotNull AbstractAudienceProvider audienceProvider,
						 final @Nullable UUID uuid,
						 final @Nullable String name) {
		this.uuid = uuid;
		this.name = name;
		if (uuid != null) {
			this.audience = audienceProvider.player(uuid);
		} else {
			this.audience = audienceProvider.console();
		}
	}

	public boolean isPlayer() {
		return this.uuid != null;
	}

	public UUID getUniqueId() {
		return Objects.requireNonNull(this.uuid, "Cannot get UUID for console sender");
	}

	public String getName() {
		return Objects.requireNonNull(this.name, "Cannot get name for console sender");
	}

	@Override
	public void sendMessage(@NotNull final Identity source,
							@NotNull final Component message,
							@NotNull final MessageType type) {
		this.audience.sendMessage(source, message, type);
	}

}
