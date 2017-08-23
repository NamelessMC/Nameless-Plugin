package com.namelessmc.namelessplugin.bungeecord.commands;

import java.util.ArrayList;
import java.util.Arrays;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.GetNotificationsCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.GetUserCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.RegisterCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.ReportCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.SetGroupCommand;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

/*
 *  CommandWithArgs/Main CMD
 */

public class CommandWithArgs extends Command {

	private NamelessPlugin plugin;
	private String permission;
	private String permissionAdmin;
	private String commandName;

	private Configuration commands;
	private String register;
	private String getNotifications;
	private String report;
	private String getUser;
	private String setGroup;

	/*
	 * Constructer
	 */
	public CommandWithArgs(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission = NamelessPlugin.permission;
		this.permissionAdmin = NamelessPlugin.permissionAdmin;

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		NamelessAPI api = plugin.getAPI();

		commands = api.getConfigManager().getCommandsConfig().getSection("Commands.SubCommand");
		register = commands.getString("Register");
		getNotifications = commands.getString("GetNotifications");
		report = commands.getString("Report");
		getUser = commands.getString("GetUser");
		setGroup = commands.getString("SetGroup");
		if (args.length == 0) {
			if (sender.hasPermission(permission + ".main")) {
				ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
					@Override
					public void run() {
						sender.sendMessage(NamelessChat.convertColors("&3&m---------------------------------"));
						sender.sendMessage(
								NamelessChat.convertColors(" &b" + plugin.getAPIUrl().split("/api")[0] + "/"));
						sender.sendMessage(NamelessChat.convertColors("&3&m---------------------------------"));
						if (sender.hasPermission(permission + ".main")) {
							sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + "&3, "
									+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_MAIN)));
						}
						if (api.getConfigManager().getConfig().getBoolean("enable-registration")) {
							if (sender.hasPermission(permission + ".register")) {
								sender.sendMessage(
										NamelessChat.convertColors("&a/" + commandName + " " + register + "&3, "
												+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REGISTER)));
							}
						}
						if (sender.hasPermission(permission + ".notifications")) {
							sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + getNotifications
									+ "&3, "
									+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETNOTIFICATIONS)));
						}
						if (api.getConfigManager().getConfig().getBoolean("enable-reports")) {
							if (sender.hasPermission(permission + ".report")) {
								sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + report
										+ "&3, " + NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REPORT)));
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
			} else {
				sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
			}
		} else if (args.length >= 1) {
			if (!commandContainsIgnoreCase(args[0])) {
				sender.sendMessage(NamelessChat.convertColors(NamelessChat
						.getMessage(NamelessMessages.INCORRECT_USAGE_MAIN).replaceAll("%command%", commandName)));
				return;
			}

			if (args[0].equalsIgnoreCase(getUser)) {
				GetUserCommand command = new GetUserCommand(plugin, commandName + " " + getUser);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
				return;
			} else if (args[0].equalsIgnoreCase(setGroup)) {
				SetGroupCommand command = new SetGroupCommand(plugin, commandName + " " + setGroup);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
				return;
			}else if (api.getConfigManager().getConfig().getBoolean("enable-registration")
						&& args[0].equalsIgnoreCase(register)) {
					RegisterCommand command = new RegisterCommand(plugin, commandName + " " + register);
					if (args.length >= 2) {
						command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(sender, newArgs);
					}
					return;
			} else if (args[0].equalsIgnoreCase(getNotifications)) {
				GetNotificationsCommand command = new GetNotificationsCommand(plugin,
						commandName + " " + getNotifications);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
				return;
			} else if (api.getConfigManager().getConfig().getBoolean("enable-reports")
					&& args[0].equalsIgnoreCase(report)) {
				ReportCommand command = new ReportCommand(plugin, commandName + " " + report);
				if (args.length >= 2) {
					command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				} else {
					String[] newArgs = new String[0];
					command.execute(sender, newArgs);
				}
				return;
			}
		}
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