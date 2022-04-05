package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.command.CommandSender;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class SpongeCommandSender extends CommandSender {

	private final @NotNull CommandSource commandSource;
	private final @NotNull Audience audience;

	public SpongeCommandSender(final @NotNull CommandSource commandSource) {
		this.commandSource = commandSource;
		this.audience = NamelessPlugin.getInstance().adventure().receiver(commandSource);
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

	@Override
	public Audience adventure() {
		return this.audience;
	}
}
