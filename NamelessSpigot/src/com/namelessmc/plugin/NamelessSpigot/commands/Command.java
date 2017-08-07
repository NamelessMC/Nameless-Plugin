package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.List;

import org.bukkit.command.PluginIdentifiableCommand;

import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;

public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {
	
	protected Command(String name) {
		super(name);
	}

	protected Command(String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
	}

	@Override
	public NamelessPlugin getPlugin() {
		return NamelessPlugin.getInstance();
	}

}