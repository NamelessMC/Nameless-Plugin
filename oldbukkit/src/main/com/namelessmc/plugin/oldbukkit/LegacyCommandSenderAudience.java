package com.namelessmc.plugin.oldbukkit;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public class LegacyCommandSenderAudience implements Audience {

	private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

	private CommandSender sender;

	LegacyCommandSenderAudience(final CommandSender sender) {
		this.sender = sender;
	}

	@Override
	public void sendMessage(final Identity source,
							final Component message,
							final MessageType type) {
		this.sender.sendMessage(SERIALIZER.serialize(message));
	}

}
