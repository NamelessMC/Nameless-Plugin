package com.namelessmc.plugin.paper;

import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.LanguageHandler;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PaperNamelessPlugin extends BukkitNamelessPlugin {

	@Override
	public void configureAudiences() {
		this.plugin.setAudienceProvider(new PaperAudienceProvider());
	}

	@Override
	public void kickPlayer(final @NonNull Player player, final LanguageHandler.@NonNull Term term) {
		player.kick(this.plugin.language().get(term));
	}

}
