package com.namelessmc.plugin.spigot.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PapiHook extends PlaceholderExpansion {

	public static PlaceholderCacher cacher;

	@Override
	public String onPlaceholderRequest(final Player player, final String identifier) {
		if (identifier.equals("notifications") && player != null) {
			if (cacher != null) {
				int count = cacher.getNotificationCount(player);
				return count > 0 ? String.valueOf(count) : "?";
			} else {
				return "<placeholders disabled>";
			}
		}
		return null;
	}

	@Override
	public @NotNull String getAuthor() {
		return String.join("Derkades");
	}

	@Override
	public @NotNull String getIdentifier() {
		return "nameless";
	}

	@Override
	public @NotNull String getVersion() {
		return "@project.version%";
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
