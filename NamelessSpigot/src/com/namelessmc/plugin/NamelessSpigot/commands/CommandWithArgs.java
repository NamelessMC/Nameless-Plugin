package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessMessages;
import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.commands.nameless.NamelessCommand;

/*
 *  CommandWithArgs/Main CMD
 */

public class CommandWithArgs extends NamelessCommand {

	private NamelessPlugin plugin;
	private String permission;
	private String permissionAdmin;
	private String commandName;

	private ConfigurationSection commands;
	private String register;
	private String getNotifications;
	private String report;
	private String getUser;
	private String setGroup;

	/*
	 * Constructer
	 */
	public CommandWithArgs(String name) {
		super(name);
		this.plugin = getPlugin();
		this.permission = NamelessPlugin.PERMISSION;
		this.permission = NamelessPlugin.PERMISSION_ADMIN;
		this.setUsage("/" + name + "<args>");
		this.setDescription(Chat.getMessage(NamelessMessages.HELP_DESCRIPTION_MAIN));

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		NamelessAPI api = plugin.getAPI();

		commands = api.getConfigManager().getCommandsConfig().getConfigurationSection("Commands.SubCommand");
		register = commands.getString("Register");
		getNotifications = commands.getString("GetNotifications");
		report = commands.getString("Report");
		getUser = commands.getString("GetUser");
		setGroup = commands.getString("SetGroup");
		if (args.length == 0) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					sender.sendMessage(NamelessChat.convertColors("&3&m---------------------------------"));
					sender.sendMessage(NamelessChat.convertColors(" &b" + plugin.getAPIUrl().split("/api")[0] + "/"));
					sender.sendMessage(NamelessChat.convertColors("&3&m---------------------------------"));
					if (sender.hasPermission(permission + ".main" + commandName.toLowerCase())) {
						sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + "&3, "
								+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_MAIN)));
					}
					if (api.getConfigManager().getConfig().getBoolean("enable-registration")) {
						if (sender.hasPermission(permission + ".register")) {
							sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + register + "&3, "
									+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REGISTER)));
						}
					}
					if (sender.hasPermission(permission + ".notifications")) {
						sender.sendMessage(
								NamelessChat.convertColors("&a/" + commandName + " " + getNotifications + "&3, "
										+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETNOTIFICATIONS)));
					}
					if (api.getConfigManager().getConfig().getBoolean("enable-reports")) {
						if (sender.hasPermission(permission + ".report")) {
							sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + report + "&3, "
									+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REPORT)));
						}
					}
					if (sender.hasPermission(permissionAdmin + ".getuser")) {
						sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + getUser + "&3, "
								+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETUSER)));
					}
					if (sender.hasPermission(permissionAdmin + ".setgroup")) {
						sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + setGroup + "&3, "
								+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_SETGROUP)));
					}
					sender.sendMessage(NamelessChat.convertColors("&3&m---------------------------------"));
				}
			});
		} else if (args.length >= 1) {
			if (!commandContainsIgnoreCase(args[0])) {
				sender.sendMessage(NamelessChat.convertColors(NamelessChat
						.getMessage(NamelessMessages.INCORRECT_USAGE_MAIN).replaceAll("%command%", commandName)));
				return true;
			}

			if (args[0].equalsIgnoreCase(getUser)) {
				if (sender.hasPermission(permissionAdmin + "." + getUser.toLowerCase())) {
					GetUserCommand command = new GetUserCommand(plugin, commandName + " " + getUser);
					if (args.length >= 2) {
						command.execute(sender, commandName + " " + getUser, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, commandName + " " + getUser, newArgs);
					}
					return true;
				} else {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
				}
			} else if (args[0].equalsIgnoreCase(setGroup)) {
				if (sender.hasPermission(permissionAdmin + "." + setGroup.toLowerCase())) {
					SetGroupCommand command = new SetGroupCommand(plugin, commandName + " " + setGroup);
					if (args.length >= 2) {
						command.execute(sender, commandName + " " + setGroup, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, commandName + " " + setGroup, newArgs);
					}
					return true;
				} else {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
				}
			} else if (api.getConfigManager().getConfig().getBoolean("enable-registration")
					&& args[0].equalsIgnoreCase(register)) {
				if (sender.hasPermission(permission + "." + register.toLowerCase())) {
					RegisterCommand command = new RegisterCommand(plugin, commandName + " " + register);
					if (args.length >= 2) {
						command.execute(sender, commandName + " " + register, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, commandName + " " + register, newArgs);
					}
				} else {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
				}
			} else if (args[0].equalsIgnoreCase(getNotifications)) {
				if (sender.hasPermission(permission + "." + getNotifications.toLowerCase())) {
					GetNotificationsCommand command = new GetNotificationsCommand(plugin,
							commandName + " " + getNotifications);
					if (args.length >= 2) {
						command.execute(sender, commandName + " " + getNotifications,
								Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, commandName + " " + getNotifications, newArgs);
					}
				} else {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
				}

			} else if (api.getConfigManager().getConfig().getBoolean("enable-reports")
					&& args[0].equalsIgnoreCase(report)) {
				if (sender.hasPermission(permission + "." + report.toLowerCase())) {
					ReportCommand command = new ReportCommand(plugin, commandName + " " + report);
					if (args.length >= 2) {
						command.execute(sender, commandName + " " + report, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, commandName + " " + report, newArgs);
					}
				} else {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
				}
			}
		}

		return true;
	}

	public boolean commandContainsIgnoreCase(String commandName) {
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
	}

}