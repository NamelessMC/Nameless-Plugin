package com.namelessmc.plugin.oldbukkit;

import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.LanguageHandler;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class OldBukkitNamelessPlugin extends BukkitNamelessPlugin {

	public OldBukkitNamelessPlugin() {
		super("oldbukkit");
		this.plugin.logger().warning("Please note that the 'Old Bukkit' version of the Nameless Plugin is not supported and may contain broken functionality.");
	}

	@Override
	protected void configureAudiences() {
		this.plugin.setAudienceProvider(new OldBukkitAudienceProvider());
	}

	@Override
	public void kickPlayer(final @NonNull Player player,
						   final LanguageHandler.@NonNull Term term) {
		final String legacyMessage = LegacyComponentSerializer.legacySection().serialize(
				this.plugin.language().get(term));
		player.kickPlayer(legacyMessage);
	}

}
