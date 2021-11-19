package com.namelessmc.plugin.common.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.UUID;

public abstract class CommandSender {

	public abstract boolean isPlayer();

	public abstract UUID getUniqueId();

	public abstract String getName();

	public void sendLegacyMessage(String legacyMessage) {
		Component c = LegacyComponentSerializer.legacySection().deserialize(legacyMessage);
		this.sendMessage(c);
	}

	public void sendMessage(final Component component) {
		this.adventure().sendMessage(component);
	}

	public abstract Audience adventure();

}
