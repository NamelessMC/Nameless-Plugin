package com.namelessmc.plugin.NamelessSpigot.hooks;

import org.bukkit.entity.Player;

import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;

import me.clip.placeholderapi.external.EZPlaceholderHook;
import xyz.derkades.derkutils.caching.Cache;

public class PapiHook extends EZPlaceholderHook {

	public PapiHook() {
		super(NamelessPlugin.getInstance(), "nameless");
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		if (!identifier.equals("notifications")) {
			return null;
		}
		
		Object cache = Cache.getCachedObject("nlmc-not" + player.getName());
		
		if (cache == null) {
			return "0";
		} else {
			return String.valueOf((int) cache);
		}
	}

}