package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Objects;

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

		CommonCommand.enabledCommands(this.plugin).forEach(command -> {
			final String permission = command.permission().toString();
			Command velocityCommand = new SimpleCommand() {
				@Override
				public void execute(final Invocation invocation) {
					final CommandSource source = invocation.source();
					final NamelessCommandSender namelessSender;
					if (source instanceof Player) {
						final Player player = (Player) source;
						namelessSender = new NamelessPlayer(source, player.getUniqueId(), player.getUsername());
					} else {
						namelessSender = new NamelessConsole(source);
					}
					command.execute(namelessSender, invocation.arguments());
				}

				@Override
				public boolean hasPermission(final Invocation invocation) {
					return invocation.source().hasPermission(permission);
				}
			};
			final String name = Objects.requireNonNull(command.actualName(),
					"command is enabled so name can't be null");
			this.server.getCommandManager().register(name, velocityCommand);
			this.registeredCommands.add(name);
		});

		this.registeredCommands.trimToSize();
	}

}
