package com.namelessmc.spigot.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import com.namelessmc.spigot.Message;
import com.namelessmc.spigot.NamelessPlugin;
import com.namelessmc.spigot.Permission;

public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {
	
	public static final Command[] COMMANDS = {
			new GetNotificationsCommand(),
			new RegisterCommand(),
			new ReportCommand(),
			new UserInfoCommand(),
			new ValidateCommand(),
	};
	
	private final String usageMessage;
	private final Permission permission;

	protected Command(final String name, final String description, final String usageMessage, final Permission permission) {
		super(name, description, usageMessage.replace("{command}", "/" + name), new ArrayList<>());
		
		this.usageMessage = usageMessage.replace("{command}", name);
		this.permission = permission;
	}

	@Override
	public NamelessPlugin getPlugin() {
		return NamelessPlugin.getInstance();
	}
	
	public String getUsageWithoutSlash() {
		return this.usageMessage;
	}
	
	@Override
	public boolean execute(final CommandSender sender, final String label, final String[] args) {
		if (!this.permission.hasPermission(sender)) {
			Message.PLAYER_SELF_NO_PERMISSION_COMMAND.send(sender);
			return true;
		}
		
		final boolean success = execute(sender, args);
		
		if (!success) {
			sender.sendMessage(this.getUsage());
		}
		
		return success;
	}
	
	public abstract boolean execute(CommandSender sender, String[] args);

}