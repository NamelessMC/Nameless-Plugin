package com.namelessmc.plugin.NamelessBungee.commands;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.NamelessPlugin;
import com.namelessmc.plugin.NamelessBungee.Permission;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


public class RegisterCommand extends Command {

	private String commandName;

	public RegisterCommand(String name) {
		super(name);
		commandName = name;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!Permission.COMMAND_REGISTER.hasPermission(sender)) {
			sender.sendMessage(Message.NO_PERMISSION.getComponents());
			return;
		}
		
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(
					Message.INCORRECT_USAGE_REGISTER.getMessage().replace("%command%", commandName)));
			return;
		}
		
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(Message.MUST_BE_INGAME.getComponents());
			return;
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			
			if (namelessPlayer.exists()) {
				sender.sendMessage(Message.HAS_REGISTERED.getComponents());
				return;
			}
			
			try {
				namelessPlayer.register(player.getName(), args[0]);
				sender.sendMessage(Message.REGISTER_SUCCESS.getComponents());
			} catch (NamelessException e) {
				if(e.getMessage().equalsIgnoreCase("Username already exists")) {
					sender.sendMessage(Message.REGISTER_EMAIL_EXISTS.getComponents());
				}else if(e.getMessage().equalsIgnoreCase("Email already exists")) {
					sender.sendMessage(Message.REGISTER_USERNAME_EXISTS.getComponents());
				}else {
					player.sendMessage(TextComponent.fromLegacyText(Message.REGISTER_FAIL.getMessage().replace("%error%", e.getMessage())));
					e.printStackTrace();
				}
			}
		});
	}

}