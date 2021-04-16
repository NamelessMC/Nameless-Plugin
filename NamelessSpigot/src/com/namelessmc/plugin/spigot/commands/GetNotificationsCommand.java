package com.namelessmc.plugin.spigot.commands;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.Notification;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class GetNotificationsCommand extends Command {

	public GetNotificationsCommand() {
		super("get-notifications",
				Term.COMMAND_NOTIFICATIONS_DESCRIPTION,
				Term.COMMAND_NOTIFICATIONS_USAGE,
				Permission.COMMAND_GET_NOTIFICATIONS);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length != 0) {
			return false;
		}

		final LanguageHandler<CommandSender> lang = NamelessPlugin.getInstance().getLanguageHandler();

		if (!(sender instanceof Player)) {
			lang.send(Term.COMMAND_NOTAPLAYER, sender);
			return true;
		}

		final Player player = (Player) sender;

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final Optional<NamelessUser> optional = NamelessPlugin.getInstance().getNamelessApi().getUser(player.getUniqueId());

				if (!optional.isPresent()) {
					lang.send(Term.PLAYER_SELF_NOTREGISTERED, sender);
					return;
				}

				final NamelessUser user = optional.get();

				final List<Notification> notifications = user.getNotifications();

				notifications.sort((n1, n2) -> n2.getType().ordinal() - n1.getType().ordinal());

				if (notifications.size() == 0) {
					lang.send(Term.COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS, sender);
					return;
				}

				Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
					notifications.forEach((notification) -> {
						final BaseComponent[] message = new ComponentBuilder(notification.getMessage())
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(lang.getMessage(Term.COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN)).create()))
							.event(new ClickEvent(ClickEvent.Action.OPEN_URL, notification.getUrl()))
							.create();
						player.spigot().sendMessage(message);
					});
				});
			} catch (final NamelessException e) {
				lang.getMessage(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL, player);
				e.printStackTrace();
				return;
			}
		});
		return true;
	}

}