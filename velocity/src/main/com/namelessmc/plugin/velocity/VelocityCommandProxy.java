package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

public class VelocityCommandProxy {

	static void registerCommands(final NamelessPlugin plugin, final ProxyServer server) {
		CommonCommand.commands(plugin).forEach(command -> {
			final String name = command.actualName();
			if (name == null) {
				// Command is disabled
				return;
			}

			Command velocityCommand = new VelocityCommand(command, plugin.audiences());
			server.getCommandManager().register(name, velocityCommand);
		});
	}

	private static class VelocityCommand implements SimpleCommand {

		private final CommonCommand command;
		private final String permission;
		private final AbstractAudienceProvider audiences;

		private VelocityCommand(final CommonCommand command, final AbstractAudienceProvider audiences) {
			this.command = command;
			this.permission = command.permission().toString();
			this.audiences = audiences;
		}

		@Override
		public void execute(final SimpleCommand.Invocation invocation) {
			final CommandSource source = invocation.source();
			final NamelessCommandSender namelessSender;
			if (source instanceof Player) {
				namelessSender = this.audiences.player(((Player) source).getUniqueId());
			} else {
				namelessSender = this.audiences.console();
			}

			if (namelessSender == null) {
				source.sendMessage(Component.text("Couldn't obtain audience for your command source"));
				return;
			}

			command.execute(namelessSender, invocation.arguments());
		}

		@Override
		public boolean hasPermission(final SimpleCommand.Invocation invocation) {
			return invocation.source().hasPermission(this.permission);
		}

	}

}
