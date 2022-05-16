package com.namelessmc.plugin.bukkit.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PapiWrapper {

	public String parse(final Player player, final String text) {
		return PlaceholderAPI.setPlaceholders(player, text);
	}

}
