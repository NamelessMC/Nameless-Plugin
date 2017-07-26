package com.namelessmc.plugin.NamelessSpigot.commands.nameless;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.Config;

public class NLCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
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
