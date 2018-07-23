package com.namelessmc.plugin.NamelessSponge.commands;

import java.io.IOException;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class NamelessCommand implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (args.length == 1 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			try {
				for (Config config : Config.values()) {
					config.reloadConfig();
				}
				
				sender.sendMessage(Chat.convertColors("&bSuccessfully reloaded all configuration files."));
			} catch (IOException e) {
				sender.sendMessage(Chat.convertColors("&4An error occured, see console log for more details."));
				e.printStackTrace();
			}
			
		} else {
			sender.sendMessage(Chat.convertColors("&4Invalid usage. Use /" + label +" reload to reload config files."));
		}
		return true;
	}
}
