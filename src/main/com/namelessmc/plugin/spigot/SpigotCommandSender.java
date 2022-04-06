package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.command.NamelessCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotCommandSender extends NamelessCommandSender {

	public SpigotCommandSender(final @NotNull AbstractAudienceProvider audiences,
							   final @NotNull CommandSender sender) {
		super(
				audiences,
				sender instanceof Player ? ((Player) sender).getUniqueId() : null,
				sender instanceof Player ? ((Player) sender).getName() : null
		);
	}

}
