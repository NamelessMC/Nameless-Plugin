package com.namelessmc.NamelessBungee.commands;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.NamelessBungee.NamelessMessages;
import com.namelessmc.NamelessBungee.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
		if (!sender.hasPermission(NamelessPlugin.PERMISSION + ".register")) {
			sender.sendMessage(NamelessMessages.NO_PERMISSION.getComponents());
			return;
		}
		
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(
					NamelessMessages.INCORRECT_USAGE_REGISTER.getMessage().replace("%command%", commandName)));
			return;
		}
		
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(NamelessMessages.MUST_BE_INGAME.getComponents());
			return;
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			
			if (namelessPlayer.exists()) {
				sender.sendMessage(NamelessMessages.REGISTER_USERNAME_EXISTS.getComponents());
				return;
			}
			
			try {
				namelessPlayer.register(player.getName(), args[0]);
			} catch (NamelessException e) {
				player.sendMessage(new ComponentBuilder("An error occured: " + e.getMessage()).color(ChatColor.RED).create());
			}
			
		});
	}

}