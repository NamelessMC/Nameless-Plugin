package com.namelessmc.plugin.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

public class PluginCommand implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!Permission.COMMAND_NAMELESS.hasPermission(sender)) {
			NamelessPlugin.getInstance().getLanguageHandler().send(Term.COMMAND_NO_PERMISSION, sender);
			return true;
		}

		if (args.length == 1 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			NamelessPlugin.getInstance().reload();
			sender.sendMessage("Successfully reloaded all configuration files.");
		} else {
			sender.sendMessage("Invalid usage. Use /" + label + " reload to reload config files.");
		}

		return true;
	}
}