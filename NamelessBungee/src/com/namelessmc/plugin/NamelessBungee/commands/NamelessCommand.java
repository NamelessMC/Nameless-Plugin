package com.namelessmc.plugin.NamelessBungee.commands;

import java.io.IOException;

import com.namelessmc.plugin.NamelessBungee.Config;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

public class NamelessCommand extends Command {

	public NamelessCommand() {
		super("nameless", null, new String[] {"nmc", "namelessmc"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			try {
				for (Config config : Config.values()) {
					config.reloadConfig();
				}
				
				sender.sendMessage(new ComponentBuilder("Successfully reloaded all configuration files.").color(ChatColor.AQUA).create());
			} catch (IOException e) {
				sender.sendMessage(new ComponentBuilder("An error occured, see console log for more details.").color(ChatColor.RED).create());
				e.printStackTrace();
			}
			
		} else {
			sender.sendMessage(new ComponentBuilder("Invalid usage. Use /nameless reload to reload config files.").color(ChatColor.RED).create());
		}
	}

}
