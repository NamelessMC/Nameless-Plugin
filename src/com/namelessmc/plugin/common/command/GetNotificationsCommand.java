package com.namelessmc.plugin.common.command;

import java.util.List;
import java.util.Optional;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.Notification;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;

public class GetNotificationsCommand extends CommonCommand {

	public GetNotificationsCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args, final String usage) {
		if (args.length != 0) {
			sender.sendMessage(usage);
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTAPLAYER));
			return;
		}

		getScheduler().runAsync(() -> {
			try {
				final Optional<NamelessUser> optional = NamelessPlugin.getInstance().getNamelessApi().getUser(sender.getUniqueId());

				if (!optional.isPresent()) {
					sender.sendMessage(getLanguage().getMessage(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				final NamelessUser user = optional.get();

				final List<Notification> notifications = user.getNotifications();

				notifications.sort((n1, n2) -> n2.getType().ordinal() - n1.getType().ordinal());

				if (notifications.size() == 0) {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS));
					return;
				}

				getScheduler().runSync(() -> {
					notifications.forEach((notification) -> {
						sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTIFICATIONS_OUTPUT_NOTIFICATION),
								"url", notification.getUrl(),
								"message", notification.getMessage());
					});
				});
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				e.printStackTrace();
				return;
			}
		});
	}

}
