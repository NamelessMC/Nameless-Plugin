package com.namelessmc.namelessplugin.spigot.utils;

import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

import me.clip.placeholderapi.external.EZPlaceholderHook;

public class PAPIPlaceholderUtil extends EZPlaceholderHook {

	NamelessPlugin plugin;

	public PAPIPlaceholderUtil(NamelessPlugin plugin) {
		super(plugin, "nameless");
		this.plugin = plugin;
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		RequestUtil request = new RequestUtil(plugin);
		if (identifier.equals("alerts")){
			try {
				return request.getAlerts(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (identifier.equals("messages")){
			try {
				return request.getPMs(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}