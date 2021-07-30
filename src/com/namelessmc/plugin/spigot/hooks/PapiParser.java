package com.namelessmc.plugin.spigot.hooks;

import org.bukkit.entity.Player;

public interface PapiParser {

	public String parse(Player player, String text);

}
