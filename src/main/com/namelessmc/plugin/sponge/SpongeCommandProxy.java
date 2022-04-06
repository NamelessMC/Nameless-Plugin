package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.namelessmc.plugin.common.command.NamelessCommandSender;
import net.kyori.adventure.text.serializer.spongeapi.SpongeComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpongeCommandProxy implements Reloadable {

	private final @NotNull ArrayList<CommandMapping> registeredCommands = new ArrayList<>();
	private final @NotNull NamelessPlugin plugin;

	SpongeCommandProxy(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		final CommandManager manager = Sponge.getCommandManager();

		for (CommandMapping mapping : this.registeredCommands) {
			manager.removeMapping(mapping);
		}
		this.registeredCommands.clear();

		CommonCommand.getEnabledCommands(this.plugin).forEach(command -> {
			final String permission = command.getPermission().toString();
			final SpongeComponentSerializer ser = SpongeComponentSerializer.get();
			final Text usage = ser.serialize(command.getUsage());
			final Text description = ser.serialize(command.getDescription());

			CommandCallable spongeCommand = new CommandCallable() {
				@Override
				public @NotNull CommandResult process(final CommandSource source,
													  final String arguments) {
					String[] args = arguments.split(" ");
					NamelessCommandSender sender = new SpongeCommandSender(plugin.audiences(), source);
					command.execute(sender, args);
					return CommandResult.success();
				}

				@Override
				public List<String> getSuggestions(final CommandSource source,
												   final String arguments,
												   final @Nullable Location<World> targetPosition) {
					return Collections.emptyList();
				}

				@Override
				public boolean testPermission(final CommandSource source) {
					return source.hasPermission(permission);
				}

				@Override
				public Optional<Text> getShortDescription(final CommandSource source) {
					return Optional.of(description);
				}

				@Override
				public Optional<Text> getHelp(final CommandSource source) {
					return Optional.empty();
				}

				@Override
				public Text getUsage(final CommandSource source) {
					return usage;
				}
			};

			manager.register(this, spongeCommand, command.getActualName()).ifPresentOrElse(
					this.registeredCommands::add,
					() -> {
						this.plugin.logger().warning("Unable to register command: " + command.getActualName());
					}
			);
		});

		this.registeredCommands.trimToSize();
	}
}
