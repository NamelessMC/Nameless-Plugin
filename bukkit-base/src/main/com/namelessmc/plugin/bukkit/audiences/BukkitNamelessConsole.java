package com.namelessmc.plugin.bukkit.audiences;

import org.bukkit.Bukkit;

import com.namelessmc.plugin.common.audiences.NamelessConsole;

import net.kyori.adventure.audience.Audience;

public class BukkitNamelessConsole extends NamelessConsole {

	public BukkitNamelessConsole(Audience audience) {
		super(audience);
	}

	@Override
	public void dispatchCommand(String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

}
