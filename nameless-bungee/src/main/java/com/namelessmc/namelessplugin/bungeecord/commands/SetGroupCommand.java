package com.namelessmc.namelessplugin.bungeecord.commands;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.utils.RequestUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 *  Register CMD
 */

public class SetGroupCommand extends Command {

	NamelessPlugin plugin;
	String permissionAdmin;

	/*
	 *  Constructer
	 */
	public SetGroupCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permissionAdmin = plugin.permissionAdmin;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permission Permission & ensure who inputted command is a Player
		if(sender.hasPermission(permissionAdmin + ".setgroup")){

			ProxiedPlayer player = (ProxiedPlayer) sender;

			// Try to register user
			ProxyServer.getInstance().getScheduler().runAsync(plugin,  new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(args.length < 2 || args.length > 2){
						player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Incorrect usage: /setgroup player groupId"));
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
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have permission to this command!"));
		}

	}
}