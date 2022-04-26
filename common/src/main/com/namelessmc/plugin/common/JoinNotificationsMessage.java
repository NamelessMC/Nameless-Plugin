package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.event.EventSubscription;
import net.md_5.bungee.config.Configuration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.namelessmc.plugin.common.LanguageHandler.Term.JOIN_NOTIFICATIONS;

public class JoinNotificationsMessage implements Reloadable {

	private final @NonNull NamelessPlugin plugin;

	private @Nullable EventSubscription subscription;

	JoinNotificationsMessage(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		if (subscription != null) {
			subscription.unsubscribe();
			subscription = null;
		}

		final Configuration conf = this.plugin.config().main();

		if (!conf.getBoolean("join-notifications")) {
			return;
		}

		this.subscription = this.plugin.events().subscribe(ServerJoinEvent.class, event ->
				onJoin(event.player().uuid()));
	}

	private void onJoin(final @NonNull UUID uuid) {
		this.plugin.scheduler().runAsync(() -> {
			this.plugin.apiProvider().api().ifPresent(api -> {
				try {
					final Optional<NamelessUser> userOptional = api.getUserByMinecraftUuid(uuid);
					if (userOptional.isEmpty()) {
						return;
					}

					final NamelessUser user = userOptional.get();
					int notifications = user.getNotificationCount();
					if (notifications == 0) {
						return;
					}

					this.plugin.scheduler().runSync(() -> {
						final String notificationsCommand = this.plugin.config().commands()
								.get("get-notifications", null);
						if (notificationsCommand == null) {
							this.plugin.logger().warning("Notifications command must be enabled for join-notifications feature");
							return;
						}

						final Audience audience = this.plugin.audiences().player(uuid);

						if (audience == null) {
							this.plugin.logger().warning("Player left before we were able to send notifications message");
							return;
						}

						final Component message = this.plugin.language()
								.get(JOIN_NOTIFICATIONS, "notifications_command", notificationsCommand);
						audience.sendMessage(message);
					});

				} catch (final NamelessException e) {
					this.plugin.logger().logException(e);
				}
			});
		});
	}
}
