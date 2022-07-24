package com.namelessmc.plugin.bukkit.audiences;

import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;

public class BukkitNamelessConsole extends NamelessConsole {

	public BukkitNamelessConsole(Audience audience) {
		super(audience);
	}

	@Override
	public void dispatchCommand(String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

}
