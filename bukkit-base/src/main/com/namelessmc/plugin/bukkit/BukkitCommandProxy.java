package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.NamelessCommandSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.derkades.derkutils.bukkit.reflection.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collections;

import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_NO_PERMISSION;

public class BukkitCommandProxy implements Reloadable {

	private final @NonNull NamelessPlugin plugin;

	private final @NonNull ArrayList<@NonNull Command> registeredCommands = new ArrayList<>();

	BukkitCommandProxy(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		for (final Command registeredCommand : this.registeredCommands) {
			ReflectionUtil.unregisterCommand(registeredCommand);
		}
		registeredCommands.clear();

		CommonCommand.commands(this.plugin).forEach(command -> {
			final String name = command.actualName();
			if (name == null) {
				// Command is disabled
				return;
			}

			final String permission = command.permission().toString();
			final PlainTextComponentSerializer ser = PlainTextComponentSerializer.plainText();
			final String usage = ser.serialize(command.usage());
			final String description = ser.serialize(command.description());
			final Component noPermissionMessage = this.plugin.language().get(COMMAND_NO_PERMISSION);
			Command spigotCommand = new Command(name, usage, description, Collections.emptyList()) {
				@Override
				public boolean execute(final CommandSender spigotSender, final String commandLabel, final String[] args) {
					final NamelessCommandSender sender;
					if (spigotSender instanceof Player) {
						sender = plugin.audiences().player(((Player) spigotSender).getUniqueId());
					} else {
						sender = plugin.audiences().console();
					}

					if (sender == null) {
						spigotSender.sendMessage("ERROR: Audience is null");
						return true;
					}

					if (!spigotSender.hasPermission(permission)) {
						sender.sendMessage(noPermissionMessage);
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
