package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpongeCommandProxy {

	static void registerCommands(final RegisterCommandEvent<Command> event,
								 final NamelessPlugin plugin,
								 final PluginContainer pluginContainer) {
		CommonCommand.commands(plugin).forEach(command -> {
			if (command == null) {
				return; // Command is disabled
			}

			final Command spongeCommand = new SpongeCommand(command);

			event.register(pluginContainer, spongeCommand, command.actualName());
		});
	}

	private static class SpongeCommand implements Command.Raw {

		private final CommonCommand command;
		private final String permission;
		private final Component usage;
		private final Component description;

		private SpongeCommand(final CommonCommand command) {
			this.command = command;
			this.permission = command.permission().toString();
			this.usage = command.usage();
			this.description = command.description();
		}

		private NamelessCommandSender causeToSender(final CommandCause cause) {
			if (cause instanceof Player) {
				final Player player = (Player) cause;
				return new NamelessPlayer(player, player.uniqueId(), player.name());
			} else if (cause instanceof SystemSubject) {
				return new NamelessConsole((SystemSubject) cause, SpongeAudienceProvider::dispatchCommand);
			} else {
				throw new UnsupportedOperationException("Unsupported command source");
			}
		}

		private String[] argsToArray(final ArgumentReader.Mutable arguments) throws ArgumentParseException {
			final String[] args = new String[arguments.totalLength()];
			for (int i = 0; i < args.length; i++) {
				args[i] = arguments.parseString();
			}
			return args;
		}

		@Override
		public CommandResult process(final CommandCause cause, final ArgumentReader.Mutable arguments) throws CommandException {
			this.command.execute(causeToSender(cause), argsToArray(arguments));
			return CommandResult.success();
		}

		@Override
		public List<CommandCompletion> complete(final CommandCause cause, final ArgumentReader.Mutable arguments) throws CommandException {
			return this.command.complete(causeToSender(cause), argsToArray(arguments)).stream()
					.map(CommandCompletion::of)
					.collect(Collectors.toList());
		}

		@Override
		public boolean canExecute(final CommandCause cause) {
			return cause.hasPermission(permission);
		}

		@Override
		public Optional<Component> shortDescription(final CommandCause cause) {
			return Optional.of(description);
		}

		@Override
		public Optional<Component> extendedDescription(final CommandCause cause) {
			return Optional.of(description);
		}

		@Override
		public Optional<Component> help(final @NonNull CommandCause cause) {
			return Raw.super.help(cause);
		}

		@Override
		public Component usage(final CommandCause cause) {
			return usage;
		}

	}

}
