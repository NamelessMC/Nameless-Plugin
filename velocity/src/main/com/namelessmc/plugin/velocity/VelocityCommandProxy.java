package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class VelocityCommandProxy implements Reloadable {

	private final @NonNull ArrayList<String> registeredCommands = new ArrayList<>();
	private final @NonNull NamelessPlugin plugin;
	private final @NonNull ProxyServer server;

	VelocityCommandProxy(final @NonNull NamelessPlugin plugin,
						 final @NonNull ProxyServer server) {
		this.plugin = plugin;
		this.server = server;
	}

	@Override
	public void reload() {
		for (String registeredName : registeredCommands) {
			this.server.getCommandManager().unregister(registeredName);
		}
		registeredCommands.clear();

		CommonCommand.commands(this.plugin).forEach(command -> {
			final String name = command.actualName();
			if (name == null) {
				// Command is disabled
				return;
			}
			final String permission = command.permission().toString();
			Command velocityCommand = new SimpleCommand() {
				@Override
				public void execute(final Invocation invocation) {
					final CommandSource source = invocation.source();
					final NamelessCommandSender namelessSender;
					if (source instanceof Player) {
						namelessSender = plugin.audiences().player(((Player) source).getUniqueId());
					} else {
						namelessSender = plugin.audiences().console();
					}

					if (namelessSender == null) {
						source.sendMessage(Component.text("Couldn't obtain audience for your command source"));
						return;
					}

					command.execute(namelessSender, invocation.arguments());
				}

				@Override
				public boolean hasPermission(final Invocation invocation) {
					return invocation.source().hasPermission(permission);
				}
			};
			this.server.getCommandManager().register(name, velocityCommand);
			this.registeredCommands.add(name);
		});

		this.registeredCommands.trimToSize();
	}

}
