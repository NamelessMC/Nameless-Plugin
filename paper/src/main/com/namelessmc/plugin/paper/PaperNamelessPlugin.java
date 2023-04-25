package com.namelessmc.plugin.paper;

import com.namelessmc.plugin.bukkit.BukkitDataSender;
import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.LanguageHandler;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PaperNamelessPlugin extends BukkitNamelessPlugin {

	public PaperNamelessPlugin() {
		super("paper");

		this.plugin.unregisterReloadable(BukkitDataSender.class);
		this.plugin.registerReloadable(new PaperDataSender(this.plugin, this));
	}

	@Override
	public void configureAudiences() {
		this.plugin.setAudienceProvider(new PaperAudienceProvider(this.plugin.config()));
	}

	@Override
	public void kickPlayer(final @NonNull Player player, final LanguageHandler.@NonNull Term term) {
		player.kick(this.plugin.language().get(term));
	}

}
