package com.namelessmc.plugin.spigot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.Permission;

public class PluginCommand implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if (!sender.hasPermission(Permission.COMMAND_NAMELESS.toString())) {
			sender.sendMessage(NamelessPlugin.getInstance().getLanguage().getComponent(Term.COMMAND_NO_PERMISSION));
			return true;
		}

		if (args.length == 1 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			NamelessPlugin.getInstance().reload();
			sender.sendMessage("Successfully reloaded all configuration files."); // TODO translate
		}

		return false;
	}
}
