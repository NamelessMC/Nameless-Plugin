package com.namelessmc.plugin.NamelessBungee.commands;

import java.util.UUID;

import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.Chat;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class GetUserCommand extends Command {

	private String commandName;

	public GetUserCommand(String commandName) {
		super(commandName);
		this.commandName = commandName;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission(NamelessPlugin.PERMISSION_ADMIN + ".getUser")) {
			sender.sendMessage(Message.NO_PERMISSION.getComponents());
			return;
		}
		
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(
					Message.INCORRECT_USAGE_GETUSER.getMessage().replace("%command%", commandName)));
			return;
		}
		
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				final String targetID = args[0];
				
				NamelessPlayer target = null;
				
				if(targetID.length() > 16) {
					target = new NamelessPlayer(UUID.fromString(targetID), NamelessPlugin.baseApiURL);
				}else {
					target = new NamelessPlayer(targetID, NamelessPlugin.baseApiURL);
				}
				
				if(!target.exists()) {
					sender.sendMessage(Message.PLAYER_NOT_FOUND.getComponents());
					return;
				}

				BaseComponent[] separator = new ComponentBuilder("--------------------------------").color(ChatColor.DARK_AQUA).italic(true).create();
				
				sender.sendMessage(separator);
				
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.GETUSER_USERNAME.getMessage().replace("%username%", target.getUsername())));
				
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.GETUSER_DISPLAYNAME.getMessage().replace("%displayname%", target.getDisplayName())));
				
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.GETUSER_UUID.getMessage().replace("%uuid%", target.getUniqueId().toString())));
				
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.GETUSER_GROUP_ID.getMessage().replace("%groupid%", "" + target.getGroupID())));
		
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.GETUSER_REGISTERED.getMessage().replace("%registereddate%", target.getRegisteredDate().toString())));
				
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.GETUSER_REPUTATION.getMessage().replace("%reputation%", "" + target.getReputations())));

				if (target.isValidated()) {
					sender.sendMessage(TextComponent.fromLegacyText(Chat.convertColorsString(
							Message.GETUSER_VALIDATED.getMessage() + "&a: " + Message.GETUSER_VALIDATED_YES.getMessage())));
				} else {
					sender.sendMessage(TextComponent.fromLegacyText(Chat.convertColorsString(
							Message.GETUSER_VALIDATED.getMessage() + "&c: " + Message.GETUSER_VALIDATED_NO.getMessage())));
				}
				
				if (target.isBanned()) {
					sender.sendMessage(TextComponent.fromLegacyText(
							Message.GETUSER_BANNED.getMessage() +
							"&c: " +
							Message.GETUSER_BANNED_YES.getMessage()));
				} else {
					sender.sendMessage(TextComponent.fromLegacyText(
							Message.GETUSER_BANNED.getMessage() +
							"&a: " +
							Message.GETUSER_BANNED_NO.getMessage()));
				}
				
				sender.sendMessage(separator);
			}
		});

	}

}