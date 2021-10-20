package com.namelessmc.plugin.bungee;

import java.util.UUID;

import com.namelessmc.plugin.common.command.CommandSender;

import net.kyori.adventure.audience.Audience;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCommandSender extends CommandSender {

	private final net.md_5.bungee.api.CommandSender sender;
	private final Audience adventure;

	public BungeeCommandSender(final net.md_5.bungee.api.CommandSender sender) {
		this.sender = sender;
		if (this.isPlayer()) {
			this.adventure = NamelessPlugin.getInstance().adventure().player(((ProxiedPlayer) sender).getUniqueId());
		} else {
			this.adventure = NamelessPlugin.getInstance().adventure().console();
		}
	}

	@Override
	public boolean isPlayer() {
		return this.sender instanceof ProxiedPlayer;
	}

	@Override
	public UUID getUniqueId() {
		throw new UnsupportedOperationException("Cannot use getUniqueId for console sender");
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException("Cannot use getName for console sender");
	}

	@Override
	public Audience adventure() {
		return this.adventure;
	}

}
