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

public class GetNotificationsCommand extends Command {

	NamelessPlugin plugin;
	String permission;

	/*
	 *  Constructer
	 */
	public GetNotificationsCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission = plugin.permission;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permission Permission & ensure who inputted command is a Player
		if(sender instanceof ProxiedPlayer && sender.hasPermission(permission + ".notifications")){

			ProxiedPlayer player = (ProxiedPlayer) sender;

			// Try to register user
			ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(args.length < 1 || args.length > 1){
						player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Incorrect usage: /register email"));
						return;
					}

					RequestUtil request = new RequestUtil(plugin);
					try {
						request.getNotifications(player);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} else if (!sender.hasPermission(permission + ".notifications")) {
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have permission to this command!"));
		} else {
			// User must be ingame to use register command
			sender.sendMessage(TextComponent.fromLegacyText("You must be ingame to use this command."));
		}
	}
}