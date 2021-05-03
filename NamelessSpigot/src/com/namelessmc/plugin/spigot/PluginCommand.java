package com.namelessmc.plugin.spigot;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.Permission;

import net.kyori.adventure.audience.Audience;

public class PluginCommand implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		final Audience adv = NamelessPlugin.getInstance().adventure().sender(sender);

		if (!sender.hasPermission(Permission.COMMAND_NAMELESS.toString())) {
			adv.sendMessage(NamelessPlugin.getInstance().getLanguage().getComponent(Term.COMMAND_NO_PERMISSION));
			return true;
		}

		if (args.length == 1 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			NamelessPlugin.getInstance().reload();
			sender.sendMessage("Successfully reloaded all configuration files."); // TODO translate
		} else {
			sender.sendMessage("Invalid usage. Use /" + label + " reload to reload config files."); // TODO translate
		}

		return true;
	}
}
