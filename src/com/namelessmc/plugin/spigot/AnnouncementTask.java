package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.Announcement;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.ApiProvider;
import com.namelessmc.plugin.common.LanguageHandler;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.derkades.derkutils.ListUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AnnouncementTask implements Runnable {

	@Override
	public void run() {
		final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();
		final ApiProvider apiProvider = NamelessPlugin.getInstance().getApiProvider();
		apiProvider.getNamelessApi().ifPresent(api -> {
			@Nullable String filterDisplay = config.getString("announcements.display");
			int delay = 0;
			for (Player player : Bukkit.getOnlinePlayers()) {
				UUID uuid = player.getUniqueId();
				String name = player.getName();
				// add delay so requests are spread out a bit
				Bukkit.getScheduler().runTaskLaterAsynchronously(NamelessPlugin.getInstance(), () -> {
					List<Announcement> announcements;
					try {
						Optional<NamelessUser> optUser = apiProvider.userFromPlayer(api, uuid, name);
						if (optUser.isPresent()) {
							announcements = optUser.get().getAnnouncements();
						} else {
							announcements = api.getAnnouncements();
						}
					} catch (NamelessException e) {
						NamelessPlugin.getInstance().getCommonLogger().logException(e);
						return;
					}
					if (filterDisplay != null) {
						announcements = announcements.stream().filter(a -> a.getDisplayPages().contains(filterDisplay)).collect(Collectors.toList());
					}
					if (!announcements.isEmpty()) {
						Announcement announcement = ListUtils.choice(announcements);
						String announcementMessage = announcement.getMessage();
						Bukkit.getScheduler().runTask(NamelessPlugin.getInstance(), () -> {
							Player player2 = Bukkit.getPlayer(uuid);
							if (player2 == null) {
								// Player left
								return;
							}
							Component message = NamelessPlugin.getInstance().getLanguage()
									.getComponent(LanguageHandler.Term.WEBSITE_ANNOUNCEMENT, "message", announcementMessage);
							NamelessPlugin.getInstance().adventure().player(player2).sendMessage(message);
						});
					}
				}, delay);
				delay += 5; // roughly 4 requests per second
			}
		});
	}

}
