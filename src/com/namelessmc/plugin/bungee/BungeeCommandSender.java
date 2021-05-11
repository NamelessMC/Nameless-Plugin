package com.namelessmc.plugin.bungee;

import java.util.UUID;

import com.namelessmc.plugin.common.command.CommandSender;

import net.kyori.adventure.audience.Audience;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCommandSender extends CommandSender {

	private final ProxiedPlayer player;
	private final Audience adventure;

	public BungeeCommandSender(final ProxiedPlayer player) {
		this.player = player;
		this.adventure = NamelessPlugin.getInstance().adventure().player(player.getUniqueId());
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public UUID getUniqueId() {
		return this.player.getUniqueId();
	}

	@Override
	public String getName() {
		return this.player.getName();
	}

	@Override
	public Audience adventure() {
		return this.adventure;
	}

}
