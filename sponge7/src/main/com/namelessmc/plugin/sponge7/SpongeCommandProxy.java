package com.namelessmc.plugin.sponge7;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.text.serializer.spongeapi.SpongeComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SpongeCommandProxy {

	static void registerCommands(final NamelessPlugin plugin, final SpongeNamelessPlugin spongePlugin) {
		final CommandManager manager = Sponge.getCommandManager();

		CommonCommand.commands(plugin).forEach(command -> {
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

					if (namelessCommandSender == null) {
						source.sendMessage(Text.of("ERROR: Cannot obtain audience for command sender"));
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

			manager.register(spongePlugin, spongeCommand, command.actualName());
		});
	}
}
