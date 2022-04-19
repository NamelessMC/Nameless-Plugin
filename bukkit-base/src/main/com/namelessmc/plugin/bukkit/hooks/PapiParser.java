package com.namelessmc.plugin.bukkit.hooks;

import org.bukkit.entity.Player;

public interface PapiParser {

	String parse(Player player, String text);

}
