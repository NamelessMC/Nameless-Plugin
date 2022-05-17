package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.text.serializer.spongeapi.SpongeComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpongeCommandProxy implements Reloadable {

	private final @NonNull ArrayList<CommandMapping> registeredCommands = new ArrayList<>();
	private final @NonNull NamelessPlugin plugin;

	SpongeCommandProxy(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		final CommandManager manager = Sponge.getCommandManager();

		for (CommandMapping mapping : this.registeredCommands) {
			manager.removeMapping(mapping);
		}
		this.registeredCommands.clear();

		CommonCommand.commands(this.plugin).forEach(command -> {
			final String name = command.actualName();
			if (name == null) {
				// Command is disabled
				return;
			}

			final String permission = command.permission().toString();
			final SpongeComponentSerializer ser = SpongeComponentSerializer.get();
			final Text usage = ser.serialize(command.usage());
			final Text description = ser.serialize(command.description());

			CommandCallable spongeCommand = new CommandCallable() {
				@Override
				public @NonNull CommandResult process(final @NonNull CommandSource source,
													  final @NonNull String arguments) {
					String[] args = arguments.split(" ");
					final NamelessCommandSender namelessCommandSender;
					if (source instanceof Player) {
						namelessCommandSender = plugin.audiences().player(((Player) source).getUniqueId());
					} else {
						namelessCommandSender = plugin.audiences().console();
					}
					command.execute(namelessCommandSender, args);
					return CommandResult.success();
				}

				@Override
				public List<String> getSuggestions(final @NonNull CommandSource source,
												   final @NonNull String arguments,
												   final @Nullable Location<World> targetPosition) {
					return Collections.emptyList();
				}

				@Override
				public boolean testPermission(final CommandSource source) {
					return source.hasPermission(permission);
				}

				@Override
				public Optional<Text> getShortDescription(final @NonNull CommandSource source) {
					return Optional.of(description);
				}

				@Override
				public Optional<Text> getHelp(final @NonNull CommandSource source) {
					return Optional.empty();
				}

				@Override
				public Text getUsage(final @NonNull CommandSource source) {
					return usage;
				}
			};

			manager.register(this, spongeCommand, command.actualName()).ifPresentOrElse(
					this.registeredCommands::add,
					() -> {
						this.plugin.logger().warning("Unable to register command: " + command.actualName());
					}
			);
		});

		this.registeredCommands.trimToSize();
	}
}
