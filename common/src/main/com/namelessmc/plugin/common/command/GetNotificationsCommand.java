package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.Notification;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class GetNotificationsCommand extends CommonCommand {

	public GetNotificationsCommand(final @NonNull NamelessPlugin plugin) {
		super(plugin,
				"get-notifications",
				COMMAND_NOTIFICATIONS_USAGE,
				COMMAND_NOTIFICATIONS_DESCRIPTION,
				Permission.COMMAND_GET_NOTIFICATIONS);
	}

	@Override
	public void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		if (args.length != 0) {
			sender.sendMessage(this.usage());
			return;
		}

		if (sender instanceof NamelessConsole) {
			sender.sendMessage(language().get(COMMAND_NOT_A_PLAYER));
			return;
		}

		scheduler().runAsync(() -> {
			final NamelessAPI api = this.apiProvider().api();
			if (api == null) {
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				return;
			}

			try {
				final NamelessUser user = api.getUserByMinecraftUuid(((NamelessPlayer) sender).uuid());

				if (user == null) {
					sender.sendMessage(language().get(PLAYER_SELF_NOT_REGISTERED));
					return;
				}

				final List<Notification> notifications = user.getNotifications();

				notifications.sort((n1, n2) -> n2.getType().ordinal() - n1.getType().ordinal());

				if (notifications.size() == 0) {
					sender.sendMessage(language().get(COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS));
					return;
				}

				scheduler().runSync(() -> {
					notifications.forEach((notification) -> {
						sender.sendMessage(language().get(COMMAND_NOTIFICATIONS_OUTPUT_NOTIFICATION,
								"url", notification.getUrl(),
								"message", notification.getMessage()));
					});
				});
			} catch (final NamelessException e) {
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				logger().logException(e);
			}
		});
	}

}
