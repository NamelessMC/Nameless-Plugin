package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.LanguageHandler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpigotNamelessPlugin extends BukkitNamelessPlugin {

	@Override
	protected void configureAudiences() {
		this.plugin.setAudienceProvider(new SpigotAudienceProvider(this));
	}

	@Override
	public void kickPlayer(final @NonNull Player player,
						   final LanguageHandler.@NonNull Term term) {
		final String legacyMessage = LegacyComponentSerializer.legacySection().serialize(
				this.plugin.language().get(term));
		player.kickPlayer(legacyMessage);
	}

}
