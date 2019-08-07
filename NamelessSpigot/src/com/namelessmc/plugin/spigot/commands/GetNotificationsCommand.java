package com.namelessmc.plugin.spigot.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.NamelessAPI.Notification;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.Message;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class GetNotificationsCommand extends Command {

	public GetNotificationsCommand() {
		super(Config.COMMANDS.getConfig().getString("get-notifications"), 
				Message.COMMAND_NOTIFICATIONS_DESCRIPTION.getMessage(), 
				Message.COMMAND_NOTIFICATIONS_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("get-notifications")),
				Permission.COMMAND_GET_NOTIFICATIONS);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length != 0) {
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return true;
		}
		
		Player player = (Player) sender;
		
		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				NamelessPlayer nameless = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
				
				if(!(nameless.exists())) {
					sender.sendMessage(Message.PLAYER_SELF_NOTREGISTERED.getMessage());
					return;
				}
				
				if (!(nameless.isValidated())) {
					sender.sendMessage(Message.PLAYER_SELF_NOTVALIDATED.getMessage());
					return;
				}
				
				// TODO Sort notifications by type
				
				List<Notification> notifications = nameless.getNotifications();
				
				if (notifications.size() == 0) {
					player.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_NONOTIFICATIONS.getMessage());
					return;
				}
				
				Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
					notifications.forEach((notification) -> {
						BaseComponent[] message = new ComponentBuilder(notification.getMessage())
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN.getMessage()).create()))
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL, notification.getUrl()))
							.create();
						player.spigot().sendMessage(message);
					});
				});
				
			} catch (NamelessException e) {
				player.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
				return;
			}
		});
		return true;
	}

}