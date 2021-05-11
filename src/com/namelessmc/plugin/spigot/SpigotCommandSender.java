package com.namelessmc.plugin.spigot;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.namelessmc.plugin.common.command.CommandSender;

import net.kyori.adventure.audience.Audience;

public class SpigotCommandSender extends CommandSender {

	private final org.bukkit.command.CommandSender sender;
	private final Audience adventure;

	public SpigotCommandSender(final org.bukkit.command.CommandSender sender) {
		this.sender = sender;
		this.adventure = NamelessPlugin.getInstance().adventure().sender(sender);
	}

	@Override
	public boolean isPlayer() {
		return this.sender instanceof Player;
	}

	@Override
	public UUID getUniqueId() {
		return ((Player) this.sender).getUniqueId();
	}

	@Override
	public String getName() {
		return this.sender.getName();
	}

	@Override
	public Audience adventure() {
		return this.adventure;
	}

}
