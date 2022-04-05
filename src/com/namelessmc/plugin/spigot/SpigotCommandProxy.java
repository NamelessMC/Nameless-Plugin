package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.derkades.derkutils.bukkit.reflection.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class SpigotCommandProxy implements Reloadable {

	private final @NotNull NamelessPlugin plugin;
	private final @NotNull NamelessPluginSpigot spigotPlugin;

	private final @NotNull ArrayList<@NotNull Command> registeredCommands = new ArrayList<>();

	SpigotCommandProxy(final @NotNull NamelessPlugin plugin,
					   final @NotNull NamelessPluginSpigot spigotPlugin) {
		this.plugin = plugin;
		this.spigotPlugin = spigotPlugin;
	}

	@Override
	public void reload() {
		for (Command registeredCommand : this.registeredCommands) {
			ReflectionUtil.unregisterCommand(registeredCommand);
		}
		registeredCommands.clear();

		CommonCommand.getEnabledCommands(this.plugin).forEach(command -> {
			final String name = Objects.requireNonNull(command.getActualName(), "Only enabled commands are returned");
			final String permission = command.getPermission().toString();

			final LegacyComponentSerializer ser = LegacyComponentSerializer.legacySection();
			final String usage = ser.serialize(command.getUsage());
			final String description = ser.serialize(command.getDescription());
			final Component noPermissionMessage = this.plugin.language().getComponent(LanguageHandler.Term.COMMAND_NO_PERMISSION);
			Command spigotCommand = new Command(name, usage, description, Collections.emptyList()) {
				@Override
				public boolean execute(final CommandSender nativeSender, final String commandLabel, final String[] args) {
					SpigotCommandSender sender = new SpigotCommandSender(spigotPlugin.adventure(), nativeSender);
					if (!nativeSender.hasPermission(permission)) {
						sender.audience().sendMessage(noPermissionMessage);
						return true;
					}
					command.execute(sender, args);
					return true;
				}
			};

			ReflectionUtil.registerCommand(name, spigotCommand);
			this.registeredCommands.add(spigotCommand);
		});

		this.registeredCommands.trimToSize();
	}

}
