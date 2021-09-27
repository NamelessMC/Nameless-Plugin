package com.namelessmc.plugin.spigot;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.namelessmc.plugin.common.command.CommandSender;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

public class SpigotCommandSender extends CommandSender {

	private final org.bukkit.command.CommandSender sender;

	public SpigotCommandSender(final org.bukkit.command.CommandSender sender) {
		this.sender = sender;
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
	
	public void sendMessage(@NotNull Component component) {
		sender.sendMessage(component);
	}

	@Override
	public Audience adventure() {
		return sender;
	}

}
