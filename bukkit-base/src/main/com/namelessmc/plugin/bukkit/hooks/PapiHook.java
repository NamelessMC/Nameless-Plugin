package com.namelessmc.plugin.bukkit.hooks;

import com.namelessmc.plugin.common.MavenConstants;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PapiHook extends PlaceholderExpansion {

	public final @NonNull PlaceholderCacher cacher;

	public PapiHook(final @NonNull PlaceholderCacher placeholderCacher) {
		this.cacher = placeholderCacher;
	}

	@Override
	public @Nullable String onPlaceholderRequest(final Player player, final String identifier) {
		if (identifier.equals("notifications") && player != null) {
			int count = cacher.getNotificationCount(player);
			return count > 0 ? String.valueOf(count) : "?";
		}
		return null;
	}

	@Override
	public @NonNull String getAuthor() {
		return "Derkades";
	}

	@Override
	public @NonNull String getIdentifier() {
		return "nameless";
	}

	@Override
	public @NonNull String getVersion() {
		return MavenConstants.PROJECT_VERSION;
	}

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

}
