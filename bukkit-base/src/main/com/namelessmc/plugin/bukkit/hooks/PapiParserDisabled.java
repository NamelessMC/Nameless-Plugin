package com.namelessmc.plugin.bukkit.hooks;

import org.bukkit.entity.Player;

public class PapiParserDisabled implements PapiParser {

	@Override
	public String parse(final Player player, final String text) {
		return text;
	}

}
