package com.namelessmc.plugin.common.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public abstract class CommandSender {

	public abstract boolean isPlayer();

	public abstract UUID getUniqueId();

	public abstract String getName();

	public void sendMessage(final Component component) {
		this.adventure().sendMessage(component);
	}

	public abstract Audience adventure();

}
