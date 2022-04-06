package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.SimpleCommand;
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

		CommonCommand.getEnabledCommands(this.plugin).forEach(command -> {
			final String permission = command.getPermission().toString();
			Command velocityCommand = new SimpleCommand() {
				@Override
				public void execute(Invocation invocation) {
					command.execute(new VelocityCommandSender(plugin.audiences(), invocation.source()), invocation.arguments());
				}

				@Override
				public boolean hasPermission(final Invocation invocation) {
					return invocation.source().hasPermission(permission);
				}
			};
			String name = command.getActualName();
			this.server.getCommandManager().register(name, velocityCommand);
			this.registeredCommands.add(name);
		});

		this.registeredCommands.trimToSize();
	}

}
