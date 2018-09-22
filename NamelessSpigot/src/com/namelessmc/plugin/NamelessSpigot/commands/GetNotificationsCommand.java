package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.NamelessAPI.Notification;
import com.namelessmc.plugin.NamelessSpigot.Config;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;
import com.namelessmc.plugin.NamelessSpigot.util.Json;
import com.namelessmc.plugin.NamelessSpigot.util.Json.ClickAction;
import com.namelessmc.plugin.NamelessSpigot.util.Json.HoverAction;
import com.namelessmc.plugin.NamelessSpigot.util.Json.JsonMessage;

public class GetNotificationsCommand extends Command {

	public GetNotificationsCommand() {
		super(Config.COMMANDS.getConfig().getString("get-notifications"), 
				Message.COMMAND_NOTIFICATIONS_DESCRIPTION.getMessage(), 
				Message.COMMAND_NOTIFICATIONS_USAGE.getMessage());
		setPermission(Permission.COMMAND_GETNOTIFICATIONS.toString());
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
					notifications.forEach((notification) -> 
						new Json().append(JsonMessage.newMessage()
								.text(notification.getMessage())
								.onHover(HoverAction.SHOW_TEXT, Message.COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN.getMessage())
								.onClick(ClickAction.OPEN_URL, notification.getUrl())).send(player)
					);
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