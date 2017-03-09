package com.namelessmc.namelessplugin.spigot.hooks;

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
		if (identifier.equals("alerts")){
			try {
				return plugin.getAPI().getPlayer(player.getName()).getNotifications().getAlerts().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (identifier.equals("messages")){
			try {
				return plugin.getAPI().getPlayer(player.getName()).getNotifications().getPMs().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}