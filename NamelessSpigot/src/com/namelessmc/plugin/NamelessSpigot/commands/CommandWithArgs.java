package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.Config;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.Permission;

public class CommandWithArgs extends Command {

	public CommandWithArgs(String name) {
		super(name, Message.HELP_DESCRIPTION_MAIN.getMessage(), "/" + name + "<args>");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		ConfigurationSection commandsConfig = Config.COMMANDS.getConfig().getConfigurationSection("subcommands");
		YamlConfiguration config = Config.MAIN.getConfig();
		
		String register = commandsConfig.getString("register");
		String getNotifications = commandsConfig.getString("get-notifications");
		String report = commandsConfig.getString("report");
		String getUser = commandsConfig.getString("get-user");
		String setGroup = commandsConfig.getString("set-group");
		
		if (args.length == 0) {
			if (!Permission.COMMAND_MAIN.hasPermission(sender)) {
				sender.sendMessage(Message.NO_PERMISSION.getMessage());
				return false;
			}
			
			String separator = Chat.convertColors("&3&m--------------------------------");

			sender.sendMessage(separator);
			//sender.sendMessage(Chat.convertColors(" &b" + NamelessPlugin.baseApiURL.toString().split("/api")[0] + "/"));
			//sender.sendMessage(separator);
			
			sender.sendMessage(Chat.convertColors("&a/" + label + "&3, " + Message.HELP_DESCRIPTION_MAIN.getMessage()));

			if (config.getBoolean("enable-registration") && Permission.COMMAND_REGISTER.hasPermission(sender)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + register + "&3, " + Message.HELP_DESCRIPTION_REGISTER.getMessage()));
			}

			if (Permission.COMMAND_GETNOTIFICATIONS.hasPermission(sender)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + getNotifications + "&3, " + Message.HELP_DESCRIPTION_GETNOTIFICATIONS.getMessage()));
			}

			if (config.getBoolean("enable-reports") && Permission.COMMAND_REPORT.hasPermission(sender)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + report + "&3, " + Message.HELP_DESCRIPTION_REPORT.getMessage()));
			}

			if (Permission.COMMAND_ADMIN_GETUSER.hasPermission(sender)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + getUser + "&3, " + Message.HELP_DESCRIPTION_GETUSER.getMessage()));
			}

			if (Permission.COMMAND_ADMIN_SETGROUP.hasPermission(sender)) {
				sender.sendMessage(Chat.convertColors("&a/" + label + " " + setGroup + "&3, " + Message.HELP_DESCRIPTION_SETGROUP.getMessage()));
			}
			sender.sendMessage(separator);
		} else if (args.length >= 1) {

			if (args[0].equalsIgnoreCase(getUser)) {
				if (Permission.COMMAND_ADMIN_GETUSER.hasPermission(sender)) {
					String cLabel = label + " " + getUser;
					UserInfoCommand command = new UserInfoCommand(cLabel);
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
				if (Permission.COMMAND_ADMIN_SETGROUP.hasPermission(sender)) {
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
				if (Permission.COMMAND_REGISTER.hasPermission(sender)) {
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
				if (Permission.COMMAND_GETNOTIFICATIONS.hasPermission(sender)) {
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
				if (Permission.COMMAND_REPORT.hasPermission(sender)) {
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