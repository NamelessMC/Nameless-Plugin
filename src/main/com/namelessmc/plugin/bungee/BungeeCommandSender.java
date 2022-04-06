package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.command.NamelessCommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

public class BungeeCommandSender extends NamelessCommandSender {

	public BungeeCommandSender(final @NotNull AbstractAudienceProvider audiences,
							   final @NotNull net.md_5.bungee.api.CommandSender sender) {
		super(
				audiences,
				sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null,
				sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getName() : null
		);
	}

}
