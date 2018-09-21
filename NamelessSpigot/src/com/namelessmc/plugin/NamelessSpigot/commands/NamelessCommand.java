package com.namelessmc.plugin.NamelessSpigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.Config;

public class NamelessCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			for (Config config : Config.values()) {
				config.reload();
			}
				
			sender.sendMessage(Chat.convertColors("&bSuccessfully reloaded all configuration files."));
			
		} else {
			sender.sendMessage(Chat.convertColors("&4Invalid usage. Use /" + label +" reload to reload config files."));
		}
		return true;
	}
}
