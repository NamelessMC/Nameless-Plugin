package com.namelessmc.plugin.NamelessBungee.commands;

import java.util.Arrays;

import com.namelessmc.plugin.NamelessBungee.Config;
import com.namelessmc.plugin.NamelessBungee.Chat;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.NamelessPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;


public class CommandWithArgs extends Command {

	private String permission;
	private String permissionAdmin;
	private String commandName;

	public CommandWithArgs(String name) {
		super(name);
		
		this.permission = NamelessPlugin.PERMISSION;
		this.permissionAdmin = NamelessPlugin.PERMISSION_ADMIN;

		commandName = name;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Configuration commandsConfig = Config.COMMANDS.getConfig().getSection("Commands.SubCommand");
		Configuration config = Config.MAIN.getConfig();
		
		String register = commandsConfig.getString("Register");
		String getNotifications = commandsConfig.getString("GetNotifications");
		String report = commandsConfig.getString("Report");
		String getUser = commandsConfig.getString("GetUser");
		String setGroup = commandsConfig.getString("SetGroup");
		
		BaseComponent[] separator = new ComponentBuilder("--------------------------------").color(ChatColor.DARK_AQUA).italic(true).create();
		
		if (args.length == 0) {
			if (!sender.hasPermission(permission + ".main")) {
				sender.sendMessage(Message.NO_PERMISSION.getComponents());
				return;
			}
			
			sender.sendMessage(separator);
				
			sender.sendMessage(TextComponent.fromLegacyText(Chat.convertColorsString(" &b" + NamelessPlugin.baseApiURL.toString().split("/api")[0] + "/")));
				
			sender.sendMessage(separator);
			
			if (sender.hasPermission(permission + ".main" + commandName.toLowerCase())) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + ChatColor.DARK_AQUA + ", " + 
						Message.HELP_DESCRIPTION_MAIN.getMessage()));
			}
						
			if (config.getBoolean("enable-registration") && sender.hasPermission(permission + ".register")) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + ChatColor.DARK_AQUA + ", " + 
						Message.HELP_DESCRIPTION_REGISTER.getMessage()));
			}
				
			if (sender.hasPermission(permission + ".notifications")) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + " " + getNotifications + ChatColor.DARK_AQUA + ", " +
						Message.HELP_DESCRIPTION_GETNOTIFICATIONS.getMessage()));
			}
				
			if (config.getBoolean("enable-reports") && sender.hasPermission(permission + ".report")) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + " " + report + ChatColor.DARK_AQUA + ", " +
						Message.HELP_DESCRIPTION_REPORT.getMessage()));
			}
				
			if (sender.hasPermission(permissionAdmin + ".getuser")) {
				sender.sendMessage(TextComponent.fromLegacyText(
						ChatColor.GREEN + "/" + commandName + " " + getUser + ChatColor.DARK_AQUA + ", " +
						Message.HELP_DESCRIPTION_GETUSER.getMessage()));
			}
				
			if (sender.hasPermission(permissionAdmin + ".setgroup")) {
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

	/*public boolean commandContainsIgnoreCase(String commandName) {
		ArrayList<String> list = new ArrayList<String>();
		if (plugin.getAPI().getConfigManager().getConfig().getBoolean("enable-registration")) {
			list.add(register);
		}
		list.add(getNotifications);
		if (plugin.getAPI().getConfigManager().getConfig().getBoolean("enable-reports")) {
			list.add(report);
		}
		list.add(getUser);
		list.add(setGroup);
		for (String command : list) {
			if (command.equalsIgnoreCase(commandName)) {
				return true;
			}
		}
		return false;
	}*/

}