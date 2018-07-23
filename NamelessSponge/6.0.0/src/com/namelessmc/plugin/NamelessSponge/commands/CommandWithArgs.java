package com.namelessmc.plugin.NamelessSponge.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.namelessmc.plugin.NamelessSponge.Chat;
import com.namelessmc.plugin.NamelessSponge.Config;
import com.namelessmc.plugin.NamelessSponge.Message;
import com.namelessmc.plugin.NamelessSponge.NamelessPlugin;
import com.namelessmc.plugin.NamelessSponge.Permission;

import ninja.leaping.configurate.ConfigurationNode;

public class CommandWithArgs implements CommandExecutor {

	private String label;

	public CommandWithArgs(String label) {
		this.label = label;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		ConfigurationNode commandsConfig = Config.COMMANDS.getConfig().getNode("subcommands");
		ConfigurationNode config = Config.MAIN.getConfig();

		String register = commandsConfig.getNode("register").getString();
		String getNotifications = commandsConfig.getNode("get-notifications").getString();
		String report = commandsConfig.getNode("report").getString();
		String getUser = commandsConfig.getNode("get-user").getString();
		String setGroup = commandsConfig.getNode("set-group").getString();

		if (!Permission.COMMAND_MAIN.hasPermission(src)) {
			src.sendMessage(Message.NO_PERMISSION.getMessage());
			return CommandResult.success();
		}

		Text separator = Chat.toText("&3&m--------------------------------");

		src.sendMessage(separator);
		src.sendMessage(Chat.toText(" &b" + NamelessPlugin.baseApiURL.toString().split("/api")[0] + "/"));
		src.sendMessage(separator);

		src.sendMessage(Chat.toText("&a/" + label + "&3, " + Message.HELP_DESCRIPTION_MAIN.getMessage()));

		if (!(src instanceof Player)) {
			if (config.getNode("enable-registration").getBoolean() && Permission.COMMAND_REGISTER.hasPermission(src)) {
				src.sendMessage(Chat.toText("&a/" + label + " " + register + "&3, " + Message.HELP_DESCRIPTION_REGISTER.getMessage()));
			}

			if (Permission.COMMAND_GETNOTIFICATIONS.hasPermission(src)) {
				src.sendMessage(Chat.toText("&a/" + label + " " + getNotifications + "&3, " + Message.HELP_DESCRIPTION_GETNOTIFICATIONS.getMessage()));
			}

			if (config.getNode("enable-reports").getBoolean() && Permission.COMMAND_REPORT.hasPermission(src)) {
				src.sendMessage(Chat.toText("&a/" + label + " " + report + "&3, " + Message.HELP_DESCRIPTION_REPORT.getMessage()));
			}
		}

		if (Permission.COMMAND_ADMIN_GETUSER.hasPermission(src)) {
			src.sendMessage(Chat.toText("&a/" + label + " " + getUser + "&3, " + Message.HELP_DESCRIPTION_GETUSER.getMessage()));
		}

		if (Permission.COMMAND_ADMIN_SETGROUP.hasPermission(src)) {
			src.sendMessage(Chat.toText("&a/" + label + " " + setGroup + "&3, " + Message.HELP_DESCRIPTION_SETGROUP.getMessage()));
		}
		src.sendMessage(separator);
		
		/*if (args.length >= 1) {

			if (args[0].equalsIgnoreCase(getUser)) {
				if (Permission.COMMAND_ADMIN_GETUSER.hasPermission(src)) {
					String cLabel = label + " " + getUser;
					GetUserCommand command = new GetUserCommand(cLabel);
					if (args.length >= 2) {
						command.execute(src, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(src, cLabel, newArgs);
					}
					return true;
				} else {
					src.sendMessage(Message.NO_PERMISSION.getMessage());
				}
			} else if (args[0].equalsIgnoreCase(setGroup)) {
				if (Permission.COMMAND_ADMIN_SETGROUP.hasPermission(src)) {
					String cLabel = label + " " + setGroup;
					SetGroupCommand command = new SetGroupCommand(cLabel);
					if (args.length >= 2) {
						command.execute(src, cLabel + " " + setGroup, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(src, cLabel + " " + setGroup, newArgs);
					}
					return true;
				} else
					src.sendMessage(Message.NO_PERMISSION.getMessage());
			} else if (config.getBoolean("enable-registration") && args[0].equalsIgnoreCase(register)) {
				if (Permission.COMMAND_REGISTER.hasPermission(src)) {
					String cLabel = label + " " + register;
					RegisterCommand command = new RegisterCommand(cLabel);
					if (args.length >= 2) {
						command.execute(src, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(src, cLabel, newArgs);
					}
				} else
					src.sendMessage(Message.NO_PERMISSION.getMessage());
			} else if (args[0].equalsIgnoreCase(getNotifications)) {
				if (Permission.COMMAND_GETNOTIFICATIONS.hasPermission(src)) {
					String cLabel = label + " " + getNotifications;
					GetNotificationsCommand command = new GetNotificationsCommand(cLabel);
					if (args.length >= 2) {
						command.execute(src, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(src, cLabel, newArgs);
					}
				} else
					src.sendMessage(Message.NO_PERMISSION.getMessage());

			} else if (config.getBoolean("enable-reports") && args[0].equalsIgnoreCase(report)) {
				if (Permission.COMMAND_REPORT.hasPermission(src)) {
					String cLabel = label + " " + report;
					ReportCommand command = new ReportCommand(cLabel);
					if (args.length >= 2) {
						command.execute(src, cLabel, Arrays.copyOfRange(args, 1, args.length));
					} else {
						String[] newArgs = new String[0];
						command.execute(src, cLabel, newArgs);
					}
				} else
					src.sendMessage(Message.NO_PERMISSION.getMessage());
			} else
				src.sendMessage(Message.INCORRECT_USAGE_MAIN.getMessage().replace("%command%", label));
		}*/

		return CommandResult.success();
	}

}