package com.namelessmc.plugin.spigot.hooks;

import org.bukkit.entity.Player;

import com.namelessmc.plugin.spigot.NamelessPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import xyz.derkades.derkutils.caching.Cache;

public class PapiHook extends PlaceholderExpansion {

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {	
		if (identifier.equals("notifications") && player != null) {
			Object cache = Cache.getCachedObject("nlmc-not" + player.getName());
			if (cache == null)
				return "?";
			else
				return String.valueOf((int) cache);
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