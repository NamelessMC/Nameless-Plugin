package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;

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

	protected Command(String name, String description, String usageMessage) {
		super(name, description, usageMessage.replace("{command}", "/" + name), new ArrayList<>());
		setPermissionMessage(Message.COMMAND_NOPERMISSION.getMessage());
		this.usageMessage = usageMessage.replace("{command}", name);
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
		boolean success = execute(sender, args);
		
		if (!success) {
			sender.sendMessage(this.getUsage());
		}
		
		return success;
	}
	
	public abstract boolean execute(CommandSender sender, String[] args);

}