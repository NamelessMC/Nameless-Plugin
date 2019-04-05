package com.namelessmc.plugin.NamelessSpigot.hooks;

import org.bukkit.entity.Player;

public class PapiParserDisabled implements PapiParser {

	@Override
	public String parse(Player player, String text) {
		return text;
	}

}
