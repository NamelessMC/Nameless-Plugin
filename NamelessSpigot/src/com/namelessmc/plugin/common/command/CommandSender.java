package com.namelessmc.plugin.common.command;

import java.util.UUID;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

public abstract class CommandSender {

//	private final boolean isPlayer;
//	private final UUID uuid;
//	private final String name;
//	private final Audience adventure;
//
//	public CommandSender(final boolean isPlayer, final UUID uuid, final String name, final Audience adventure) {
//		this.isPlayer = isPlayer;
//		this.uuid = uuid;
//		this.name = name;
//		this.adventure = Objects.requireNonNull(adventure, "Adventure audience must not be null");
//		if (isPlayer) {
//			Objects.requireNonNull(uuid, "UUID must not be null for player command senders");
//			Objects.requireNonNull(name, "Name must not be null for player command senders");
//		}
//	}

//	public boolean isPlayer() {
//		return this.isPlayer;
//	}
//
//	public UUID getUniqueId() {
//		if (!this.isPlayer) {
//			throw new IllegalStateException("Can't get UUID for console sender");
//		}
//		return this.uuid;
//	}
//
//	public String getName() {
//		if (!this.isPlayer) {
//			throw new IllegalStateException("Can't get name for console sender");
//		}
//
//		return this.name;
//	}

	public abstract boolean isPlayer();

	public abstract UUID getUniqueId();

	public abstract String getName();

	public void sendMessage(final String message) {
		this.adventure().sendMessage(MiniMessage.get().parse(message));
	}

	public void sendMessage(final String message, final String... placeholders) {
		this.adventure().sendMessage(MiniMessage.get().parse(message, placeholders));
	}

	public abstract Audience adventure();

//	public Audience adventure() {
//		return this.adventure;
//	}

}
