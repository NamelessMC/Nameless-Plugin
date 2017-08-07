package com.namelessmc.plugin.NamelessBungee.commands;

import java.util.Arrays;

import com.namelessmc.plugin.NamelessBungee.Config;
import com.namelessmc.plugin.NamelessBungee.Chat;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.NamelessPlugin;
import com.namelessmc.plugin.NamelessBungee.Permission;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;


public class CommandWithArgs extends Command {
	
	private String commandName;

	public CommandWithArgs(String name) {
		super(name);
		commandName = name;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Configuration commandsConfig = Config.COMMANDS.getConfig().getSection("subcommands");
		Configuration config = Config.MAIN.getConfig();
		
		String register = commandsConfig.getString("register");
		String getNotifications = commandsConfig.getString("get-notifications");
		String report = commandsConfig.getString("report");
		String getUser = commandsConfig.getString("get-user");
		String setGroup = commandsConfig.getString("set-group");
		
		BaseComponent[] separator = new ComponentBuilder("--------------------------------").color(ChatColor.DARK_AQUA).italic(true).create();
		
		if (args.length == 0) {
			if (!Permission.COMMAND_MAIN.hasPermission(sender)) {
				sender.sendMessage(Message.NO_PERMISSION.getComponents());
				return;
			}
			
			sender.sendMessage(separator);
				
			sender.sendMessage(TextComponent.fromLegacyText(Chat.convertColorsString(" &b" + NamelessPlugin.baseApiURL.toString().split("/api")[0] + "/")));
				
			sender.sendMessage(separator);
			
			sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + ChatColor.DARK_AQUA
						+ ", " + Message.HELP_DESCRIPTION_MAIN.getMessage()));
						
			if (config.getBoolean("enable-registration") && Permission.COMMAND_REGISTER.hasPermission(sender)) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + ChatColor.DARK_AQUA + ", " + 
						Message.HELP_DESCRIPTION_REGISTER.getMessage()));
			}
				
			if (Permission.COMMAND_GETNOTIFICATIONS.hasPermission(sender)) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + " " + getNotifications + ChatColor.DARK_AQUA + ", " +
						Message.HELP_DESCRIPTION_GETNOTIFICATIONS.getMessage()));
			}
				
			if (config.getBoolean("enable-reports") && Permission.COMMAND_REPORT.hasPermission(sender)) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + " " + report + ChatColor.DARK_AQUA + ", " +
						Message.HELP_DESCRIPTION_REPORT.getMessage()));
			}
				
			if (Permission.COMMAND_ADMIN_GETUSER.hasPermission(sender)) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + " " + getUser + ChatColor.DARK_AQUA + ", " +
						Message.HELP_DESCRIPTION_GETUSER.getMessage()));
			}
				
			if (Permission.COMMAND_ADMIN_SETGROUP.hasPermission(sender)) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + " " + setGroup + ChatColor.DARK_AQUA + ", " + 
						Message.HELP_DESCRIPTION_SETGROUP.getMessage()));
			}
						
			sender.sendMessage(separator);
		} else if (args.length >= 1) {
			if (args[0].equalsIgnoreCase(getUser)) {
				GetUserCommand command = new GetUserCommand(commandName + " " + getUser);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
			} else if (args[0].equalsIgnoreCase(setGroup)) {
				SetGroupCommand command = new SetGroupCommand(commandName + " " + setGroup);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
			} else if (args[0].equalsIgnoreCase(register) && config.getBoolean("enable-registration")) {
					RegisterCommand command = new RegisterCommand(commandName + " " + register);
					if (args.length >= 2) {
						command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, newArgs);
					}
			} else if (args[0].equalsIgnoreCase(getNotifications)) {
				GetNotificationsCommand command = new GetNotificationsCommand(commandName + " " + getNotifications);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
			} else if (config.getBoolean("enable-reports")
					&& args[0].equalsIgnoreCase(report)) {
				ReportCommand command = new ReportCommand(commandName + " " + report);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
			} else {
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.INCORRECT_USAGE_MAIN.getMessage().replace("%command%", commandName)));
			}
		}
	}

}