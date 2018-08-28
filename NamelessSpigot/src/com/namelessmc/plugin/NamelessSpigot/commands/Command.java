package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.ArrayList;

import org.bukkit.command.PluginIdentifiableCommand;

import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;

public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {
	
	protected Command(String name) {
		super(name);
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
	}

	protected Command(String name, String description, String usageMessage) {
		super(name, description, usageMessage, new ArrayList<>());
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
	}

	@Override
	public NamelessPlugin getPlugin() {
		return NamelessPlugin.getInstance();
	}

}