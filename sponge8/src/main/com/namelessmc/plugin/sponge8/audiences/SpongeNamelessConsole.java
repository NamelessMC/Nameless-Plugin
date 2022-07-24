package com.namelessmc.plugin.sponge8.audiences;

import com.namelessmc.plugin.common.audiences.NamelessConsole;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;

public class SpongeNamelessConsole extends NamelessConsole {

	public SpongeNamelessConsole() {
		super(Sponge.systemSubject());
	}

	@Override
	public void dispatchCommand(String command) {
		try {
			Sponge.server().commandManager().process(Sponge.systemSubject(), command);
		} catch (CommandException e) {
			throw new RuntimeException(e);
		}
	}

}
