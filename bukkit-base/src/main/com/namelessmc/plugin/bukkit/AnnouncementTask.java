package com.namelessmc.plugin.bukkit;

import com.namelessmc.java_api.Announcement;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.derkades.derkutils.ListUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

		Configuration config = this.plugin.config().main();
		if (config.getBoolean("announcements.enabled")) {
			Duration interval = Duration.parse(config.getString("announcements.interval"));
			this.task = this.plugin.scheduler().runTimer(this, interval);
		}
	}

	@Override
	public void run() {
		final Configuration config = this.plugin.config().main();
		final ApiProvider apiProvider = this.plugin.apiProvider();
		apiProvider.api().ifPresent(api -> {
			@Nullable String filterDisplay = config.getString("announcements.display");
			Duration delay = Duration.ZERO;
			for (Player player : Bukkit.getOnlinePlayers()) {
				UUID uuid = player.getUniqueId();
				// add delay so requests are spread out a bit
				this.plugin.scheduler().runDelayed(() -> {
					this.plugin.scheduler().runAsync(() -> {
						List<Announcement> announcements;
						try {
							Optional<NamelessUser> optUser = api.getUserByMinecraftUuid(uuid);
							if (optUser.isPresent()) {
								announcements = optUser.get().getAnnouncements();
							} else {
								announcements = api.getAnnouncements();
							}
						} catch (NamelessException e) {
							this.plugin.logger().logException(e);
							return;
						}
						if (filterDisplay != null) {
							announcements = announcements.stream().filter(a -> a.getDisplayPages().contains(filterDisplay)).collect(Collectors.toList());
						}
						if (!announcements.isEmpty()) {
							Announcement announcement = ListUtils.choice(announcements);
							String announcementMessage = announcement.getMessage();
							this.plugin.scheduler().runSync(() -> {
								NamelessPlayer player2 = this.plugin.audiences().player(uuid);
								if (player2 == null) {
									// Player left
									return;
								}
								final Component message = this.plugin.language().get(
										LanguageHandler.Term.WEBSITE_ANNOUNCEMENT, "message", announcementMessage);
								player2.sendMessage(message);
							});
						}
					});
				}, delay);
				delay = delay.plusMillis(250); // 4 requests per second
			}
		});
	}

}
