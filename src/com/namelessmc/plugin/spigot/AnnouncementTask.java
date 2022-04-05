package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.Announcement;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.ApiProvider;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.derkades.derkutils.ListUtils;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnnouncementTask implements Runnable, Reloadable {

	private final @NotNull NamelessPlugin plugin;
	private @Nullable AbstractScheduledTask task;

	AnnouncementTask(final @NotNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		if (task != null) {
			task.cancel();
			task = null;
		}

		Configuration config = this.plugin.config().getMainConfig();
		if (config.getBoolean("announcements.enabled")) {
			Duration interval = Duration.parse(config.getString("announcements.interval"));
			this.task = this.plugin.scheduler().runTimer(this, interval);
		}
	}

	@Override
	public void run() {
		final Configuration config = this.plugin.config().getMainConfig();
		final ApiProvider apiProvider = this.plugin.api();
		apiProvider.getNamelessApi().ifPresent(api -> {
			@Nullable String filterDisplay = config.getString("announcements.display");
			Duration delay = Duration.ZERO;
			for (Player player : Bukkit.getOnlinePlayers()) {
				UUID uuid = player.getUniqueId();
				String name = player.getName();
				// add delay so requests are spread out a bit
				this.plugin.scheduler().runDelayed(() -> {
					this.plugin.scheduler().runAsync(() -> {
						List<Announcement> announcements;
						try {
							Optional<NamelessUser> optUser = apiProvider.userFromPlayer(api, uuid, name);
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
								Player player2 = Bukkit.getPlayer(uuid);
								if (player2 == null) {
									// Player left
									return;
								}
								String message = this.plugin.language()
										.getLegacyMessage(LanguageHandler.Term.WEBSITE_ANNOUNCEMENT, "message", announcementMessage);
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
