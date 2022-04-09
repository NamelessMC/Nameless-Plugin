package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.Notification;
import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class GetNotificationsCommand extends CommonCommand {

	public GetNotificationsCommand(final @NotNull NamelessPlugin plugin) {
		super(plugin,
				"get-notifications",
				Term.COMMAND_NOTIFICATIONS_USAGE,
				Term.COMMAND_NOTIFICATIONS_DESCRIPTION,
				Permission.COMMAND_GET_NOTIFICATIONS);
	}

	@Override
	public void execute(final @NotNull NamelessCommandSender sender, final @NotNull String@NotNull[] args) {
		if (args.length != 0) {
			sender.sendMessage(this.getUsage());
			return;
		}

		if (sender instanceof NamelessConsole) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOT_A_PLAYER));
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (optApi.isEmpty()) {
				sender.sendMessage(getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final Optional<NamelessUser> optional = api.getUser(((NamelessPlayer) sender).getUniqueId());

				if (optional.isEmpty()) {
					sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_NOT_REGISTERED));
					return;
				}

				final NamelessUser user = optional.get();

				final List<Notification> notifications = user.getNotifications();

				notifications.sort((n1, n2) -> n2.getType().ordinal() - n1.getType().ordinal());

				if (notifications.size() == 0) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS));
					return;
				}

				getScheduler().runSync(() -> {
					notifications.forEach((notification) -> {
						sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_NOTIFICATION,
								"url", notification.getUrl(),
								"message", notification.getMessage()));
					});
				});
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				getLogger().logException(e);
			}
		});
	}

}
