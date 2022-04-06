package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.command.CommandSender;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SpigotCommandSender extends CommandSender {

	private final org.bukkit.command.CommandSender sender;

	public SpigotCommandSender(final @NotNull BukkitAudiences audiences,
							   final @NotNull org.bukkit.command.CommandSender sender) {
		super(audiences.sender(sender));
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

}
