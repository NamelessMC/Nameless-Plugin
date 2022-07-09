package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.event.NamelessJoinEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.event.EventSubscription;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.UUID;

import static com.namelessmc.plugin.common.LanguageHandler.Term.JOIN_NOT_REGISTERED;

public class JoinNotRegisteredMessage implements Reloadable {

	private final @NonNull NamelessPlugin plugin;

	private @Nullable EventSubscription subscription;

	JoinNotRegisteredMessage(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void unload() {
		if (subscription != null) {
			subscription.unsubscribe();
			subscription = null;
		}
	}

	@Override
	public void load() {
		final ConfigurationNode conf = this.plugin.config().main();

		if (conf.node("not-registered-join-message").getBoolean()) {
			this.subscription = this.plugin.events().subscribe(NamelessJoinEvent.class, event ->
					onJoin(event.player().uuid()));
		}
	}

	private void onJoin(final @NonNull UUID uuid) {
		this.plugin.scheduler().runAsync(() -> {
			final NamelessAPI api = this.plugin.apiProvider().api();
			if (api == null) {
				return;
			}

			NamelessUser user;
			try {
				user = api.userByMinecraftUuid(uuid);
			} catch (final NamelessException e) {
				this.plugin.logger().logException(e);
				return;
			}

			if (user == null) {
				this.plugin.scheduler().runSync(() -> {
					Audience audience = this.plugin.audiences().player(uuid);
					if (audience != null) {
						final Component message = this.plugin.language().get(JOIN_NOT_REGISTERED);
						audience.sendMessage(message);
					}
				});
			}
		});
	}

}
