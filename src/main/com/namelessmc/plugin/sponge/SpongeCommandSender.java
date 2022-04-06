package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.AbstractAudienceProvider;
import com.namelessmc.plugin.common.command.NamelessCommandSender;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;

public class SpongeCommandSender extends NamelessCommandSender {

	public SpongeCommandSender(final @NotNull AbstractAudienceProvider audiences,
							   final @NotNull CommandSource commandSource) {
		super(
				audiences,
				commandSource instanceof Player ? ((Player) commandSource).getUniqueId() : null,
				commandSource instanceof Player ? ((Player) commandSource).getUsername() : null
		);
	}

}
