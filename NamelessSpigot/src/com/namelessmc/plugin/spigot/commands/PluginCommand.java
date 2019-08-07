package com.namelessmc.plugin.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.Message;
import com.namelessmc.plugin.spigot.Permission;

public class PluginCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!Permission.COMMAND_NAMELESS.hasPermission(sender)) {
			Message.PLAYER_SELF_NO_PERMISSION_COMMAND.send(sender);
			return true;
		}
		
		if (args.length == 1 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
			for (Config config : Config.values()) {
				config.reload();
			}
				
			sender.sendMessage("Successfully reloaded all configuration files.");
		} else {
			sender.sendMessage("Invalid usage. Use /" + label + " reload to reload config files.");
		}
		
		return true;
	}
}
