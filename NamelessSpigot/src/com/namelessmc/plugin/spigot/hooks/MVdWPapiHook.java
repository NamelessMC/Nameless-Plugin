package com.namelessmc.plugin.spigot.hooks;

import com.namelessmc.plugin.spigot.NamelessPlugin;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import xyz.derkades.derkutils.caching.Cache;

public class MVdWPapiHook {

	public void hook() {
		PlaceholderAPI.registerPlaceholder(NamelessPlugin.getInstance(), "nameless_notifications", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent event) {			
				Object cache = Cache.getCachedObject("nlmc-not" + event.getOfflinePlayer().getName());
				
				if (cache == null) {
					return "0";
				} else {
					return String.valueOf((int) cache);
				}
			}
		});
	}

}