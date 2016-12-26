package com.namelessmc.namelessplugin.spigot.utils;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public class MVdWPlaceholderUtil {

	NamelessPlugin plugin;

	public MVdWPlaceholderUtil(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public void hook(){
		RequestUtil request = new RequestUtil(plugin);
		PlaceholderAPI.registerPlaceholder(plugin, "nameless_alerts", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
				try {
					return request.getAlerts(event.getPlayer());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		PlaceholderAPI.registerPlaceholder(plugin, "nameless_messages", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
				try {
					return request.getPMs(event.getPlayer());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	}

}