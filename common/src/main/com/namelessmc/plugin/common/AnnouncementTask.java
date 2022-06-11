package com.namelessmc.plugin.common;

import com.namelessmc.java_api.Announcement;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import xyz.derkades.derkutils.ListUtils;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.namelessmc.plugin.common.LanguageHandler.Term.WEBSITE_ANNOUNCEMENT;

public class AnnouncementTask implements Runnable, Reloadable {

	private final @NonNull NamelessPlugin plugin;
	private @Nullable AbstractScheduledTask task;

	AnnouncementTask(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		if (task != null) {
			task.cancel();
			task = null;
		}

		final CommentedConfigurationNode config = this.plugin.config().main().node("announcements");
		if (config.node("enabled").getBoolean()) {
			final Duration interval = ConfigurationHandler.getDuration(config.node("interval"));
			if (interval != null) {
				this.task = this.plugin.scheduler().runTimer(this, interval);
			} else {
				this.plugin.logger().warning("Invalid announcements interval");
			}
		}
	}

	@Override
	public void run() {
		final CommentedConfigurationNode config = this.plugin.config().main().node("announcements");
		final NamelessAPI api = this.plugin.apiProvider().api();
		if (api == null) {
			return;
		}
		final @Nullable String filterDisplay = config.node("display").getString();
		Duration delay = Duration.ZERO;
		for (final NamelessPlayer player : this.plugin.audiences().onlinePlayers()) {
			// add delay so requests are spread out a bit
			this.plugin.scheduler().runDelayed(() -> {
				this.plugin.scheduler().runAsync(() -> {
					List<Announcement> announcements;
					try {
						final NamelessUser user = api.userByMinecraftUuid(player.uuid());
						if (user != null) {
							announcements = user.announcements();
						} else {
							announcements = api.announcements();
						}
					} catch (NamelessException e) {
						this.plugin.logger().logException(e);
						return;
					}
					if (filterDisplay != null) {
						announcements = announcements.stream().filter(a -> a.displayedPages().contains(filterDisplay)).collect(Collectors.toList());
					}
					if (!announcements.isEmpty()) {
						Announcement announcement = ListUtils.choice(announcements);
						String announcementMessage = announcement.message();
						this.plugin.scheduler().runSync(() -> {
							final NamelessPlayer player2 = this.plugin.audiences().player(player.uuid());
							if (player2 == null) {
								// Player left
								return;
							}
							final Component message = this.plugin.language().get(
									WEBSITE_ANNOUNCEMENT, "message", announcementMessage);
							player2.sendMessage(message);
						});
					}
				});
			}, delay);
			delay = delay.plusMillis(250); // 4 requests per second
		}
	}

}
