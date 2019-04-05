package com.namelessmc.plugin.NamelessSpigot.hooks;

import org.bukkit.entity.Player;

public interface PapiParser {
	
	public String parse(Player player, String text);

}
