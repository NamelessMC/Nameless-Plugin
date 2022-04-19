package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.LanguageHandler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpigotNamelessPlugin extends BukkitNamelessPlugin {

	@Override
	protected void configureAudiences() {
		this.plugin.setAudienceProvider(new SpigotAudienceProvider(this));
	}

	@Override
	public void kickPlayer(@NotNull Player player, LanguageHandler.@NotNull Term term) {
		final String legacyMessage = LegacyComponentSerializer.legacySection().serialize(
				this.plugin.language().getComponent(term));
		player.kickPlayer(legacyMessage);
	}

}
