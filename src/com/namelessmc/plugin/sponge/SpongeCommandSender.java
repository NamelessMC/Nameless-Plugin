package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.command.CommandSender;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class SpongeCommandSender extends CommandSender {

	private final @NotNull CommandSource commandSource;

	public SpongeCommandSender(final @NotNull SpongeAudiences audiences,
							   final @NotNull CommandSource commandSource) {
		super(audiences.receiver(commandSource));
		this.commandSource = commandSource;
	}

	@Override
	public boolean isPlayer() {
		return commandSource instanceof Player;
	}

	@Override
	public UUID getUniqueId() {
		if (this.isPlayer()) {
			return ((Player) this.commandSource).getUniqueId();
		}
		throw new IllegalStateException("Cannot get UUID for console sender");
	}

	@Override
	public String getName() {
		if (this.isPlayer()) {
			return ((Player) this.commandSource).getName();
		}
		throw new IllegalStateException("Cannot get name for console sender");
	}

}
