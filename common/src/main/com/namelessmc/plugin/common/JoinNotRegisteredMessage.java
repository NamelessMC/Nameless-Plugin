package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.event.ServerJoinEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.event.EventSubscription;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.namelessmc.plugin.common.LanguageHandler.Term.JOIN_NOT_REGISTERED;

public class JoinNotRegisteredMessage implements Reloadable {

	private final @NotNull NamelessPlugin plugin;

	private @Nullable EventSubscription subscription;

	JoinNotRegisteredMessage(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		if (subscription != null) {
			subscription.unsubscribe();
			subscription = null;
		}

		final Configuration conf = this.plugin.config().main();

		if (!conf.getBoolean("not-registered-join-message")) {
			return;
		}

		this.subscription = this.plugin.events().subscribe(ServerJoinEvent.class, event ->
				onJoin(event.player().uuid()));
	}

	private void onJoin(final @NotNull UUID uuid) {
		this.plugin.scheduler().runAsync(() -> {
			this.plugin.apiProvider().api().ifPresent(api -> {
				Optional<NamelessUser> userOptional;
				try {
					userOptional = api.getUser(uuid);
				} catch (final NamelessException e) {
					this.plugin.logger().logException(e);
					return;
				}

				if (userOptional.isEmpty()) {
					this.plugin.scheduler().runSync(() -> {
						Audience audience = this.plugin.audiences().player(uuid);
						if (audience != null) {
							final Component message = this.plugin.language().get(JOIN_NOT_REGISTERED);
							audience.sendMessage(message);
						}
					});
				}
			});
		});
	}

}
