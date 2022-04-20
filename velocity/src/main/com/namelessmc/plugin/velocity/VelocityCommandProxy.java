package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VelocityCommandProxy implements Reloadable {

	private final @NotNull ArrayList<String> registeredCommands = new ArrayList<>();
	private final @NotNull NamelessPlugin plugin;
	private final @NotNull ProxyServer server;

	VelocityCommandProxy(final @NotNull NamelessPlugin plugin,
						 final @NotNull ProxyServer server) {
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
			String name = command.actualName();
			this.server.getCommandManager().register(name, velocityCommand);
			this.registeredCommands.add(name);
		});

		this.registeredCommands.trimToSize();
	}

}
