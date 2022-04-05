package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
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
	private final @NotNull NamelessPluginSponge spongePlugin;

	SpongeCommandProxy(final @NotNull NamelessPlugin plugin,
					   final @NotNull NamelessPluginSponge spongePlugin) {
		this.plugin = plugin;
		this.spongePlugin = spongePlugin;
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
				public CommandResult process(CommandSource source, String arguments) throws CommandException {
					String[] args = arguments.split(" ");
					CommandSender sender = new SpongeCommandSender(spongePlugin.adventure(), source);
					command.execute(sender, args);
					return CommandResult.success();
				}

				@Override
				public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
					return Collections.emptyList();
				}

				@Override
				public boolean testPermission(CommandSource source) {
					return source.hasPermission(permission);
				}

				@Override
				public Optional<Text> getShortDescription(CommandSource source) {
					return Optional.of(description);
				}

				@Override
				public Optional<Text> getHelp(CommandSource source) {
					return Optional.empty();
				}

				@Override
				public Text getUsage(CommandSource source) {
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
