package com.namelessmc.plugin.spigot.hooks;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

public class PapiParserEnabled implements PapiParser {

	@Override
	public String parse(final Player player, final String text) {
		return PlaceholderAPI.setPlaceholders(player, text);
	}

}
