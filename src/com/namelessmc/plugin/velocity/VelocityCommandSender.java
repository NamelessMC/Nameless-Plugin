package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.command.CommandSender;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class VelocityCommandSender extends CommandSender {

	private final @NotNull CommandSource source;

	VelocityCommandSender(final @NotNull CommandSource source) {
		this.source = source;
	}

	@Override
	public boolean isPlayer() {
		return this.source instanceof Player;
	}

	@Override
	public UUID getUniqueId() {
		if (this.source instanceof Player) {
			return ((Player) this.source).getUniqueId();
		} else {
			throw new UnsupportedOperationException("getUniqueId() is only supported for player sources");
		}
	}

	@Override
	public String getName() {
		if (this.source instanceof Player) {
			return ((Player) this.source).getUsername();
		} else {
			throw new UnsupportedOperationException("getName() is only supported for player sources");
		}
	}

	@Override
	public Audience audience() {
		return this.source;
	}

}
