package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.Config;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.commands.nameless.NamelessCommand;

/*
 *  CommandWithArgs/Main CMD
 */

public class CommandWithArgs extends NamelessCommand {

	/*
	 * Constructer
	 */
	public CommandWithArgs(String name) {
		super(name);
		this.setUsage("/" + name + "<args>");
		this.setDescription(Message.HELP_DESCRIPTION_MAIN.getMessage());
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		ConfigurationSection commandsConfig = Config.COMMANDS.getConfig().getConfigurationSection("commands.subcommands");
		YamlConfiguration config = Config.MAIN.getConfig();
		
		String register = commandsConfig.getString("Register");
		String getNotifications = commandsConfig.getString("GetNotifications");
		String report = commandsConfig.getString("Report");
		String getUser = commandsConfig.getString("GetUser");
		String setGroup = commandsConfig.getString("SetGroup");
		
		String separator = Chat.convertColors("&3&m--------------------------------");
		
		if (args.length == 0) {
			if (!sender.hasPermission(NamelessPlugin.PERMISSION_MAIN)) {
				sender.sendMessage(Message.NO_PERMISSION.getMessage());
				return false;
			}

			sender.sendMessage(separator);
			sender.sendMessage(Chat.convertColors(" &b" + NamelessPlugin.baseApiURL.toString().split("/api")[0] + "/"));
			sender.sendMessage(separator);
			
			sender.sendMessage(Chat.convertColors("&a/" + label + "&3, " + Message.HELP_DESCRIPTION_MAIN.getMessage()));

			if (config.getBoolean("enable-registration") && sender.hasPermission(NamelessPlugin.PERMISSION_REGISTER)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + register + "&3, " + Message.HELP_DESCRIPTION_REGISTER.getMessage()));
			}

			if (sender.hasPermission(NamelessPlugin.PERMISSION_NOTIFICATIONS)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + getNotifications + "&3, " + Message.HELP_DESCRIPTION_GETNOTIFICATIONS.getMessage()));
			}

			if (config.getBoolean("enable-reports") && sender.hasPermission(NamelessPlugin.PERMISSION_REPORT)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + report + "&3, " + Message.HELP_DESCRIPTION_REPORT.getMessage()));
			}

			if (sender.hasPermission(NamelessPlugin.PERMISSION_ADMIN_GETUSER)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + getUser + "&3, " + Message.HELP_DESCRIPTION_GETUSER.getMessage()));
			}

			if (sender.hasPermission(NamelessPlugin.PERMISSION_ADMIN_SETGROUP)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + setGroup + "&3, " + Message.HELP_DESCRIPTION_SETGROUP.getMessage()));
			}
			sender.sendMessage(separator);
		} else if (args.length >= 1) {

			if (args[0].equalsIgnoreCase(getUser)) {
				if (sender.hasPermission(NamelessPlugin.PERMISSION_ADMIN_GETUSER)) {
					String cLabel = label + " " + getUser;
					GetUserCommand command = new GetUserCommand(cLabel);
					if (args.length >= 2) {
						command.execute(sender, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, cLabel, newArgs);
					}
					return true;
				} else {
					sender.sendMessage(Message.NO_PERMISSION.getMessage());
				}
			} else if (args[0].equalsIgnoreCase(setGroup)) {
				if (sender.hasPermission(NamelessPlugin.PERMISSION_ADMIN_SETGROUP)) {
					String cLabel = label + " " + setGroup;
					SetGroupCommand command = new SetGroupCommand(cLabel);
					if (args.length >= 2) {
						command.execute(sender, cLabel + " " + setGroup, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, cLabel + " " + setGroup, newArgs);
					}
					return true;
				} else sender.sendMessage(Message.NO_PERMISSION.getMessage());
			} else if (config.getBoolean("enable-registration")	&& args[0].equalsIgnoreCase(register)) {
				if (sender.hasPermission(NamelessPlugin.PERMISSION_REGISTER)) {
					String cLabel = label + " " + register;
					RegisterCommand command = new RegisterCommand(cLabel);
					if (args.length >= 2) {
						command.execute(sender, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, cLabel, newArgs);
					}
				} else sender.sendMessage(Message.NO_PERMISSION.getMessage());
			} else if (args[0].equalsIgnoreCase(getNotifications)) {
				if (sender.hasPermission(NamelessPlugin.PERMISSION_NOTIFICATIONS)) {
					String cLabel = label + " " + getNotifications;
					GetNotificationsCommand command = new GetNotificationsCommand(cLabel);
					if (args.length >= 2) {
						command.execute(sender, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, cLabel, newArgs);
					}
				} else sender.sendMessage(Message.NO_PERMISSION.getMessage());

			} else if (config.getBoolean("enable-reports") && args[0].equalsIgnoreCase(report)) {
				if (sender.hasPermission(NamelessPlugin.PERMISSION_REPORT)) {
					String cLabel = label + " " + report;
					ReportCommand command = new ReportCommand(cLabel);
					if (args.length >= 2) {
						command.execute(sender, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, cLabel, newArgs);
					}
				} else sender.sendMessage(Message.NO_PERMISSION.getMessage());
			} else sender.sendMessage(Message.INCORRECT_USAGE_MAIN.getMessage().replace("%command%", label));
		}

		return true;
	}

}