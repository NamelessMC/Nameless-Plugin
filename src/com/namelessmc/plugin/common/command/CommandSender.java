package com.namelessmc.plugin.common.command;

import java.util.UUID;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

public abstract class CommandSender {

	public abstract boolean isPlayer();

	public abstract UUID getUniqueId();

	public abstract String getName();

	public void sendMessage(final String message) {
		this.adventure().sendMessage(MiniMessage.miniMessage().parse(message));
	}

	public void sendMessage(final String message, final String... placeholders) {
		this.adventure().sendMessage(MiniMessage.miniMessage().parse(message, placeholders));
	}

	public abstract Audience adventure();

}
