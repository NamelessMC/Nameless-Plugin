package com.namelessmc.plugin.paper;

import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperNamelessPlugin extends BukkitNamelessPlugin {

	@Override
	public void configureAudiences() {
		this.plugin.setAudienceProvider(new PaperAudienceProvider());
	}

	@Override
	public void kickPlayer(final @NotNull Player player, final LanguageHandler.@NotNull Term term) {
		player.kick(this.plugin.language().get(term));
	}

}
