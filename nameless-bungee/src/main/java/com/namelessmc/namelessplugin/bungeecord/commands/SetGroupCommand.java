package com.namelessmc.namelessplugin.bungeecord.commands;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayerSetGroup;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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

			// Try to register user
			ProxyServer.getInstance().getScheduler().runAsync(plugin,  new Runnable(){
				@Override
				public void run(){
					
					NamelessAPI api = plugin.getAPI();
					NamelessPlayer namelessPlayer = api.getPlayer(args[0]);
					NamelessChat chat = api.getChat();
					
					// Ensure email is set
					if(args.length < 2 || args.length > 2){
						sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.INCORRECT_USAGE_SETGROUP)));
					}else{
						NamelessPlayerSetGroup group = namelessPlayer.setGroupID(args[1]);
						String success = chat.getMessage(NamelessMessages.SETGROUP_SUCCESS);
						success.replaceAll("%player%", group.getID());
						success.replaceAll("%previousgroup%", group.getPreviousGroup().toString());
						success.replaceAll("%newgroup%", group.getNewGroup().toString());
						
						if(group.hasError()){
							sender.sendMessage(chat.convertColors("&cError: " + group.getErrorMessage()));
						}else{
							sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.SETGROUP_SUCCESS).replaceAll("%player%", group.getID())));
						}
					}
				}
			});

		} else if (!sender.hasPermission(permissionAdmin + ".setgroup")) {
			NamelessAPI api = plugin.getAPI();
			NamelessChat chat = api.getChat();
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.NO_PERMISSION)));
		}

	}
}