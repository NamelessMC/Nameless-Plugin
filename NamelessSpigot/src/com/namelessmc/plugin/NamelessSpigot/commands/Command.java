package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;

public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {
	
	public static final Command[] COMMANDS = {
			new GetNotificationsCommand(),
			new RegisterCommand(),
			new ReportCommand(),
			new SetGroupCommand(),
			new UserInfoCommand(),
			new ValidateCommand(),
	};
	
	/*protected Command(String name) {
		super(name);
		setPermissionMessage(Message.COMMAND_NOPERMISSION.getMessage());
	}*/
	
	private String usageMessage;
	private Permission permission;

	protected Command(String name, String description, String usageMessage, Permission permission) {
		super(name, description, usageMessage.replace("{command}", "/" + name), new ArrayList<>());
		
		this.usageMessage = usageMessage.replace("{command}", name);
		this.permission = permission;
	}

	@Override
	public NamelessPlugin getPlugin() {
		return NamelessPlugin.getInstance();
	}
	
	public String getUsageWithoutSlash() {
		return usageMessage;
	}
	
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!permission.hasPermission(sender)) {
			Message.PLAYER_SELF_NO_PERMISSION_COMMAND.send(sender);
			return true;
		}
		
		boolean success = execute(sender, args);
		
		if (!success) {
			sender.sendMessage(this.getUsage());
		}
		
		return success;
	}
	
	public abstract boolean execute(CommandSender sender, String[] args);

}