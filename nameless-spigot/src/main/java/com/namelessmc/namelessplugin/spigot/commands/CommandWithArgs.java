package com.namelessmc.namelessplugin.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

/*
 *  CommandWithArgs/Main CMD
 */

public class CommandWithArgs extends Command implements PluginIdentifiableCommand {

	NamelessPlugin plugin;
	String permission;
	String permissionAdmin;

	/*
	 * Constructer
	 */
	public CommandWithArgs(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission = plugin.permission;
		this.permission = plugin.permissionAdmin;
		this.usageMessage = "/" + name + "<args>";
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		
		// Will do here.
		
		return true;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}