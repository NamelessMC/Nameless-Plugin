package com.namelessmc.spigot.commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.command.CommandSender;

import com.namelessmc.spigot.Chat;
import com.namelessmc.spigot.Config;
import com.namelessmc.spigot.Message;

public class SubCommands extends org.bukkit.command.Command {

	public SubCommands() {
		super(Config.COMMANDS.getConfig().getString("subcommands.name", "website"),
				"",
				"/" + Message.COMMAND_SUBCOMMANDS_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("subcommands.name", "website")),
				new ArrayList<>());
	}

	@Override
	public boolean execute(final CommandSender sender, final String label, final String[] args) {
		if (args.length > 0) {
			final String subcommand = args[0];

			for (final Command command : Command.COMMANDS) {
				if (command.getName().equalsIgnoreCase(subcommand)) {
					return command.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
				}
			}
		}

		final String separator = Chat.convertColors("&3&m--------------------------------");

		sender.sendMessage(separator);
		sender.sendMessage("");

		for (final Command command : Command.COMMANDS) {
			if (command.getName().equals("disabled")) {
				continue;
			}

			sender.sendMessage(Message.COMMAND_SUBCOMMANDS_HELP_PREFIX.getMessage("command", this.getName())
					+ " " + command.getUsageWithoutSlash());
			sender.sendMessage(command.getDescription());
			sender.sendMessage("");
		}

		sender.sendMessage(separator);

		return true;
	}

}