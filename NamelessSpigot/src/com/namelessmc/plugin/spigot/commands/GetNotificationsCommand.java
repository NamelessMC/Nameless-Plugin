package com.namelessmc.plugin.spigot.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.Notification;
import com.namelessmc.java_api.UserNotExistException;
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
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length != 0) {
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return true;
		}
		
		final Player player = (Player) sender;
		
		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final NamelessUser user = NamelessPlugin.getInstance().api.getUser(player.getUniqueId());
				
				// TODO Sort notifications by type
				
				final List<Notification> notifications = user.getNotifications();
				
				if (notifications.size() == 0) {
					player.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_NONOTIFICATIONS.getMessage());
					return;
				}
				
				Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
					notifications.forEach((notification) -> {
						final BaseComponent[] message = new ComponentBuilder(notification.getMessage())
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN.getMessage()).create()))
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL, notification.getUrl()))
							.create();
						player.spigot().sendMessage(message);
					});
				});
			} catch (final UserNotExistException e) {
				sender.sendMessage(Message.PLAYER_SELF_NOTREGISTERED.getMessage());
			} catch (final NamelessException e) {
				player.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
				return;
			}
		});
		return true;
	}

}