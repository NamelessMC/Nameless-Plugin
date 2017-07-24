package com.namelessmc.plugin.NamelessBungee.commands;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class GetNotificationsCommand extends Command {

	private String commandName;

	public GetNotificationsCommand(String commandName) {
		super(commandName);
		this.commandName = commandName;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission(NamelessPlugin.PERMISSION + ".notifications")) {
			sender.sendMessage(Message.NO_PERMISSION.getComponents());
			return;
		}
		
		if (args.length != 0) {
			sender.sendMessage(TextComponent.fromLegacyText(
					Message.INCORRECT_USAGE_GETNOTIFICATIONS.getMessage().replace("%command%", commandName)));
			return;
		}
		
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(Message.MUST_BE_INGAME.getComponents());
			return;
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer nameless = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			
			int messages;
			int alerts;
			
			try {
				messages = nameless.getMessageCount();
				alerts = nameless.getAlertCount();
			} catch (NamelessException e) {
				player.sendMessage(new ComponentBuilder("An error occured: " + e.getMessage()).color(ChatColor.RED).create());
				return;
			}
			
			BaseComponent[] pmMessage = TextComponent.fromLegacyText(
					Message.PM_NOTIFICATIONS_MESSAGE.getMessage().replace("%pms%", "" + messages));
			BaseComponent[] alertMessage = TextComponent.fromLegacyText(
					Message.ALERTS_NOTIFICATIONS_MESSAGE.getMessage().replace("%alerts%", "" + alerts));
			BaseComponent[] noNotifications = Message.NO_NOTIFICATIONS.getComponents();

			if (alerts == 0 && messages == 0) {
				sender.sendMessage(noNotifications);
			} else if (alerts == 0) {
				sender.sendMessage(pmMessage);
			} else if (messages == 0) {
				sender.sendMessage(alertMessage);
			} else {
				sender.sendMessage(alertMessage);
				sender.sendMessage(pmMessage);
			}
		});
	}

}