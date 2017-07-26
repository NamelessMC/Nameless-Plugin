package com.namelessmc.plugin.NamelessSpigot.hooks;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public class MVdWPlaceholderUtil {

	NamelessPlugin plugin;

	public MVdWPlaceholderUtil(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public void hook() {
		PlaceholderAPI.registerPlaceholder(plugin, "nameless_alerts", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
				NamelessPlayer namelessPlayer = new NamelessPlayer(event.getPlayer().getUniqueId(), NamelessPlugin.baseApiURL);
				if (namelessPlayer.exists()) {
					try {
						return Integer.toString(namelessPlayer.getAlertCount());
					} catch (NamelessException e) {
						e.printStackTrace();
					}
				}else {
					return "0";
				}
				return null;
			}
		});
		PlaceholderAPI.registerPlaceholder(plugin, "nameless_messages", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
				NamelessPlayer namelessPlayer = new NamelessPlayer(event.getPlayer().getUniqueId(), NamelessPlugin.baseApiURL);
				if (namelessPlayer.exists()) {
					try {
						return Integer.toString(namelessPlayer.getMessageCount());
					} catch (NamelessException e) {
						e.printStackTrace();
					}
				}else {
					return "0";
				}
				return null;
			}
		});
	}

}