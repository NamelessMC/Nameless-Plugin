package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessCommandSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.derkades.derkutils.bukkit.reflection.ReflectionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class BukkitCommandProxy implements Reloadable {

	private final @NotNull NamelessPlugin plugin;

	private final @NotNull ArrayList<@NotNull Command> registeredCommands = new ArrayList<>();

	BukkitCommandProxy(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
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

			final PlainTextComponentSerializer ser = PlainTextComponentSerializer.plainText();
			final String usage = ser.serialize(command.getUsage());
			final String description = ser.serialize(command.getDescription());
			final Component noPermissionMessage = this.plugin.language().getComponent(LanguageHandler.Term.COMMAND_NO_PERMISSION);
			Command spigotCommand = new Command(name, usage, description, Collections.emptyList()) {
				@Override
				public boolean execute(final CommandSender spigotSender, final String commandLabel, final String[] args) {
					final NamelessCommandSender sender;
					if (spigotSender instanceof Player) {
						sender = plugin.audiences().player(((Player) spigotSender).getUniqueId());
					} else {
						sender = plugin.audiences().console();
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
