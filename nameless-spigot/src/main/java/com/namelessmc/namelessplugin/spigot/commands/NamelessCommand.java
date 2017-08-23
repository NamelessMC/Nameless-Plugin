package com.namelessmc.namelessplugin.spigot.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

public abstract class NamelessCommand extends Command implements PluginIdentifiableCommand {
	protected NamelessCommand(String name) {
		super(name);
	}

	protected NamelessCommand(String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
	}

	@Override
	public NamelessPlugin getPlugin() {
		return NamelessPlugin.getInstance();
	}
}
