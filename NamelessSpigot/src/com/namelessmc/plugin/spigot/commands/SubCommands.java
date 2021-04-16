package com.namelessmc.plugin.spigot.commands;

import java.util.Arrays;
import java.util.Collections;

import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.Chat;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.NamelessPlugin;

public class SubCommands extends org.bukkit.command.Command {

	public SubCommands() {
		super(Config.COMMANDS.getConfig().getString("subcommands.name", "website"),
				"",
				"/" + NamelessPlugin.getInstance().getLanguageHandler().getMessage(Term.COMMAND_SUBCOMMANDS_USAGE, "command", Config.COMMANDS.getConfig().getString("subcommands.name", "website")),
				Collections.emptyList());
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

		final LanguageHandler<CommandSender> lang = NamelessPlugin.getInstance().getLanguageHandler();

		final String separator = Chat.convertColors("&3&m--------------------------------");

		sender.sendMessage(separator);
		sender.sendMessage("");

		for (final Command command : Command.COMMANDS) {
			if (command.getName().equals("disabled")) {
				continue;
			}

			sender.sendMessage(lang.getMessage(Term.COMMAND_SUBCOMMANDS_HELP_PREFIX, sender, "command", this.getName())
					+ " " + command.getUsageWithoutSlash());
			sender.sendMessage(command.getDescription());
			sender.sendMessage("");
		}

		sender.sendMessage(separator);

		return true;
	}

}