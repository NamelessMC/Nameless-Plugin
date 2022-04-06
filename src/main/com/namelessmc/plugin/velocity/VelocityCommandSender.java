package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.command.NamelessCommandSender;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

public class VelocityCommandSender extends NamelessCommandSender {

	VelocityCommandSender(final @NotNull AbstractAudienceProvider audienceProvider,
						  final @NotNull CommandSource source) {
		super(
				audienceProvider,
				source instanceof Player ? ((Player) source).getUniqueId() : null,
				source instanceof Player ? ((Player) source).getUsername() : null
		);
	}

}
