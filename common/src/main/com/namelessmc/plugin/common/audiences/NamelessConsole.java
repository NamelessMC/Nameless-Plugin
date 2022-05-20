package com.namelessmc.plugin.common.audiences;

import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

public class NamelessConsole extends NamelessCommandSender {

	private final @NonNull Consumer<String> commandDispatcher;

	public NamelessConsole(final @NonNull Audience audience, final @NonNull Consumer<String> commandDispatcher) {
		super(audience);
		this.commandDispatcher = commandDispatcher;
	}

	public void dispatchCommand(final String command) {
		this.commandDispatcher.accept(command);
	}

}
