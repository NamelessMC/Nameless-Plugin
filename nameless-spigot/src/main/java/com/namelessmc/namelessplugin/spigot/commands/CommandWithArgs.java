package com.namelessmc.namelessplugin.spigot.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.alone.GetNotificationsCommand;
import com.namelessmc.namelessplugin.spigot.commands.alone.GetUserCommand;
import com.namelessmc.namelessplugin.spigot.commands.alone.RegisterCommand;
import com.namelessmc.namelessplugin.spigot.commands.alone.ReportCommand;
import com.namelessmc.namelessplugin.spigot.commands.alone.SetGroupCommand;

/*
 *  CommandWithArgs/Main CMD
 */

public class CommandWithArgs extends NamelessCommand {

	NamelessPlugin plugin;
	String permission;
	String permissionAdmin;
	String commandName;

	/*
	 * Constructer
	 */
	public CommandWithArgs(String name) {
		super(name);
		this.plugin = getPlugin();
		this.permission = NamelessPlugin.permission;
		this.permission = NamelessPlugin.permissionAdmin;
		this.usageMessage = "/" + name + "<args>";

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		NamelessAPI api = plugin.getAPI();

		ConfigurationSection commands = api.getConfigManager().getCommandsConfig()
				.getConfigurationSection("Commands.SubCommand");
		String register = commands.getString("Register");
		String getNotifications = commands.getString("GetNotifications");
		String report = commands.getString("Report");
		String getUser = commands.getString("GetUser");
		String setGroup = commands.getString("SetGroup");
		if (args.length == 0) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					sender.sendMessage(NamelessChat.convertColors("&3&m--------------------------------"));
					sender.sendMessage(NamelessChat.convertColors(" &b" + plugin.getAPIUrl().split("/api")[0]));
					sender.sendMessage(NamelessChat.convertColors("&3&m--------------------------------"));
					if (sender.hasPermission(NamelessPlugin.permission + "." + commandName.toLowerCase())) {
						sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + "&3, "
								+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_MAIN)));
					}
					if (sender.hasPermission(NamelessPlugin.permission + "." + register.toLowerCase())) {
						sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + register + "&3, "
								+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REGISTER)));
					}
					if (sender.hasPermission(NamelessPlugin.permission + "." + getNotifications.toLowerCase())) {
						sender.sendMessage(
								NamelessChat.convertColors("&a/" + commandName + " " + getNotifications + "&3, "
										+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETNOTIFICATIONS)));
					}
					if (api.getConfigManager().getConfig().getBoolean("enable-reports")) {
						if (sender.hasPermission(NamelessPlugin.permission + "." + report.toLowerCase())) {
							sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + report + "&3, "
									+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REPORT)));
						}
					}
					if (sender.hasPermission(NamelessPlugin.permissionAdmin + "." + getUser.toLowerCase())) {
						sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + getUser + "&3, "
								+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETUSER)));
					}
					if (sender.hasPermission(NamelessPlugin.permissionAdmin + "." + setGroup.toLowerCase())) {
						sender.sendMessage(NamelessChat.convertColors("&a/" + commandName + " " + setGroup + "&3, "
								+ NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_SETGROUP)));
					}
					sender.sendMessage(NamelessChat.convertColors("&3&m--------------------------------"));
				}
			});
		} else if (args.length >= 1) {
			if (!commands.contains(args[0])) {
				sender.sendMessage(NamelessChat.convertColors(NamelessChat
						.getMessage(NamelessMessages.INCORRECT_USAGE_MAIN).replaceAll("%command%", commandName)));
			}

			if (args[0].equalsIgnoreCase(getUser)) {
				if (sender.hasPermission(NamelessPlugin.permissionAdmin + "." + getUser.toLowerCase())) {
					GetUserCommand command = new GetUserCommand(plugin, commandName + " " + getUser);
					command.execute(sender, label, Arrays.copyOfRange(args, 2, args.length));
				} else {
					NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION));
				}
			} else if (args[0].equalsIgnoreCase(setGroup)) {
				if (sender.hasPermission(NamelessPlugin.permissionAdmin + "." + setGroup.toLowerCase())) {
					SetGroupCommand command = new SetGroupCommand(plugin, commandName + " " + setGroup);
					command.execute(sender, label, Arrays.copyOfRange(args, 2, args.length));
				} else {
					NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION));
				}
			}

			if (sender instanceof Player) {
				if (args[0].equalsIgnoreCase(register)) {
					if (sender.hasPermission(NamelessPlugin.permission + "." + register.toLowerCase())) {
						RegisterCommand command = new RegisterCommand(plugin, commandName + " " + register);
						command.execute(sender, label, Arrays.copyOfRange(args, 2, args.length));
					} else {
						NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION));
					}
				} else if (args[0].equalsIgnoreCase(getNotifications)) {
					if (sender.hasPermission(NamelessPlugin.permission + "." + getNotifications.toLowerCase())) {
						GetNotificationsCommand command = new GetNotificationsCommand(plugin,
								commandName + " " + getNotifications);
						command.execute(sender, label, Arrays.copyOfRange(args, 2, args.length));
					} else {
						NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION));
					}

				} else if (api.getConfigManager().getConfig().getBoolean("enable-reports")
						&& args[0].equalsIgnoreCase(report)) {
					if (sender.hasPermission(NamelessPlugin.permission + "." + report.toLowerCase())) {
						ReportCommand command = new ReportCommand(plugin, commandName + " " + report);
						command.execute(sender, label, Arrays.copyOfRange(args, 2, args.length));
					} else {
						NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION));
					}
				}
			} else {
				sender.sendMessage(
						NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_BE_INGAME)));
			}

		}

		return true;
	}
}