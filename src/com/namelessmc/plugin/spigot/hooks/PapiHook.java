package com.namelessmc.plugin.spigot.hooks;

import org.bukkit.entity.Player;

import com.namelessmc.plugin.spigot.NamelessPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PapiHook extends PlaceholderExpansion {

	@Override
	public String onPlaceholderRequest(final Player player, final String identifier) {
		if (identifier.equals("notifications") && player != null) {
			final Integer notifs = PlaceholderCacher.CACHED_NOTIFICATION_COUNT.get(player.getUniqueId());
			return notifs == null ? "?" : String.valueOf(notifs);
		}
		return null;
	}

	@Override
	public String getAuthor() {
		return String.join(", ", NamelessPlugin.getInstance().getDescription().getAuthors());
	}

	@Override
	public String getIdentifier() {
		return "nameless";
	}

	@Override
	public String getVersion() {
		return NamelessPlugin.getInstance().getDescription().getVersion();
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