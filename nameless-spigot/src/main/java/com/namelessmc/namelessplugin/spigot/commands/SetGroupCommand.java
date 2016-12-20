package com.namelessmc.namelessplugin.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.utils.MessagesUtil;
import com.namelessmc.namelessplugin.spigot.utils.RequestUtil;

/*
 *  Register CMD
 */

public class SetGroupCommand implements CommandExecutor {

	NamelessPlugin plugin;
	String permissionAdmin;

	/*
	 *  Constructer
	 */
	public SetGroupCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
		this.permissionAdmin = plugin.permissionAdmin;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		MessagesUtil messages = new MessagesUtil(plugin);
		// check if player has permission Permission & ensure who inputted command is a Player
		if(sender.hasPermission(permissionAdmin + ".setgroup")){

			Player player = (Player) sender;

			// Try to register user
			Bukkit.getScheduler().runTaskAsynchronously(plugin,  new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(args.length < 2 || args.length > 2){
						player.sendMessage(ChatColor.RED + "Incorrect usage: /setgroup player groupId");
						return;
					}

					RequestUtil request = new RequestUtil(plugin);
					try {
						request.setGroup(args[0], args[1]);;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} else if (!sender.hasPermission(permissionAdmin + ".setgroup")) {
			sender.sendMessage(messages.getMessage("NO_PERMISSION"));
		}

		return true;
	}
}