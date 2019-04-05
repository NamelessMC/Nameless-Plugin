package com.namelessmc.plugin.NamelessSpigot.hooks;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class PapiParserEnabled implements PapiParser {
	
	@Override
	public String parse(Player player, String text) {
		return PlaceholderAPI.setPlaceholders(player, text);
	}

}
