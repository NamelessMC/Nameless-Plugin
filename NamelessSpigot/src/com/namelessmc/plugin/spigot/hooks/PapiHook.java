package com.namelessmc.plugin.spigot.hooks;

import java.util.Optional;

import org.bukkit.entity.Player;

import com.namelessmc.plugin.spigot.NamelessPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import xyz.derkades.derkutils.caching.Cache;

public class PapiHook extends PlaceholderExpansion {

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (identifier.equals("notifications") && player != null) {
			final Optional<Integer> cache = Cache.get("nlmc-not" + player.getName());
			if (cache.isEmpty()) {
				return "?";
			} else {
				return String.valueOf(cache.get());
			}
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