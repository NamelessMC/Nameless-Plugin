package com.namelessmc.namelessplugin.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerNotifications;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerSetGroup;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessReportPlayer;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;

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
	public CommandWithArgs(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission = plugin.permission;
		this.permission = plugin.permissionAdmin;
		this.usageMessage = "/" + name + "<args>";

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		NamelessAPI api = plugin.getAPI();
		NamelessChat chat = api.getChat();

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
					sender.sendMessage(chat.convertColors("&3&m--------------------------------"));
					sender.sendMessage(chat.convertColors(" &b" + plugin.getAPIUrl().split("/api")[0]));
					sender.sendMessage(chat.convertColors("&3&m--------------------------------"));
					if (sender.hasPermission(plugin.permission + "." + commandName.toLowerCase())) {
						sender.sendMessage(chat.convertColors("&a/" + commandName + "&3, "
								+ chat.getMessage(NamelessMessages.HELP_DESCRIPTION_MAIN)));
					}
					if (sender.hasPermission(plugin.permission + "." + register.toLowerCase())) {
						sender.sendMessage(chat.convertColors("&a/" + commandName + " " + register + "&3, "
								+ chat.getMessage(NamelessMessages.HELP_DESCRIPTION_REGISTER)));
					}
					if (sender.hasPermission(plugin.permission + "." + getNotifications.toLowerCase())) {
						sender.sendMessage(chat.convertColors("&a/" + commandName + " " + getNotifications + "&3, "
								+ chat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETNOTIFICATIONS)));
					}
					if (sender.hasPermission(plugin.permission + "." + report.toLowerCase())) {
						sender.sendMessage(chat.convertColors("&a/" + commandName + " " + report + "&3, "
								+ chat.getMessage(NamelessMessages.HELP_DESCRIPTION_REPORT)));
					}
					if (sender.hasPermission(plugin.permissionAdmin + "." + getUser.toLowerCase())) {
						sender.sendMessage(chat.convertColors("&a/" + commandName + " " + getUser + "&3, "
								+ chat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETUSER)));
					}
					if (sender.hasPermission(plugin.permissionAdmin + "." + setGroup.toLowerCase())) {
						sender.sendMessage(chat.convertColors("&a/" + commandName + " " + setGroup + "&3, "
								+ chat.getMessage(NamelessMessages.HELP_DESCRIPTION_SETGROUP)));
					}
					sender.sendMessage(chat.convertColors("&3&m--------------------------------"));
				}
			});
		} else if (args.length >= 1) {
			if (!commands.contains(args[0])) {
				sender.sendMessage(plugin.getAPI().getChat().convertColors(plugin.getAPI().getChat()
						.getMessage(NamelessMessages.INCORRECT_USAGE_MAIN).replaceAll("%command%", commandName)));

			}
			if (sender instanceof Player) {
				Player player = (Player) sender;
				NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
				if (args[0].equalsIgnoreCase(register)) {
					if (sender.hasPermission(plugin.permission + "." + register.toLowerCase())) {
						if (args.length == 1 || args.length > 2) {
							sender.sendMessage(plugin.getAPI().getChat()
									.convertColors(plugin.getAPI().getChat()
											.getMessage(NamelessMessages.INCORRECT_USAGE_REGISTER)
											.replaceAll("%command%", commandName + " " + register)));
						} else {
							Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
								@Override
								public void run() {
									api.registerPlayer(player, args[0]);
								}
							});
						}
					} else {
						plugin.getAPI().getChat()
								.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION));
					}
				} else if (args[0].equalsIgnoreCase(getNotifications)) {
					if (sender.hasPermission(plugin.permission + "." + getNotifications.toLowerCase())) {
						if (args.length > 1) {
							sender.sendMessage(plugin.getAPI().getChat()
									.convertColors(plugin.getAPI().getChat()
											.getMessage(NamelessMessages.INCORRECT_USAGE_GETNOTIFICATIONS)
											.replaceAll("%command%", commandName + " " + getNotifications)));
						} else {
							if (namelessPlayer.exists()) {

								// Try to getNotifications
								Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
									@Override
									public void run() {
										NamelessPlayerNotifications notifications = namelessPlayer.getNotifications();
										Integer pms = notifications.getPMs();
										Integer alerts = notifications.getAlerts();
										String errorMessage = notifications.getErrorMessage();
										boolean hasError = notifications.hasError();

										String pmMessage = chat.getMessage(NamelessMessages.PM_NOTIFICATIONS_MESSAGE)
												.replaceAll("%pms%", pms.toString());
										String alertMessage = chat
												.getMessage(NamelessMessages.ALERTS_NOTIFICATIONS_MESSAGE)
												.replaceAll("%alerts%", alerts.toString());
										String noNotifications = chat.getMessage(NamelessMessages.NO_NOTIFICATIONS);

										if (hasError) {
											// Error with request
											player.sendMessage(chat.convertColors("&4Error: " + errorMessage));
										} else if (alerts.equals(0) && pms.equals(0)) {
											player.sendMessage(chat.convertColors(noNotifications));
										} else if (alerts.equals(0)) {
											player.sendMessage(chat.convertColors(pmMessage));
										} else if (pms.equals(0)) {
											player.sendMessage(chat.convertColors(alertMessage));
										} else {
											player.sendMessage(chat.convertColors(alertMessage));
											player.sendMessage(chat.convertColors(pmMessage));
										}
									}
								});
							} else {
								player.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.MUST_REGISTER)));
							}
						}
					} else {
						plugin.getAPI().getChat()
								.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION));
					}

				} else if (args[0].equalsIgnoreCase(report)) {
					if (sender.hasPermission(plugin.permission + "." + report.toLowerCase())) {
						if (namelessPlayer.exists()) {
							if (args.length <= 2) {
								sender.sendMessage(plugin.getAPI().getChat()
										.convertColors(plugin.getAPI().getChat()
												.getMessage(NamelessMessages.INCORRECT_USAGE_REPORT)
												.replaceAll("%command%", commandName + " " + report)));
							} else {
								Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
									@Override
									public void run() {
										NamelessReportPlayer report = namelessPlayer.reportPlayer(args);

										if (report.hasError()) {
											// Error with request
											player.sendMessage(
													chat.convertColors("&4Error: " + report.getErrorMessage()));
										} else {
											// Display success message to user
											player.sendMessage(
													chat.convertColors(chat.getMessage(NamelessMessages.REPORT_SUCCESS)
															.replaceAll("%player%", args[0])));
										}
									}
								});
							}
						} else {
							plugin.getAPI().getChat().convertColors(
									plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION));
						}
					}
				}
			} else {
				sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.MUST_BE_INGAME)));
			}

			if (args[0].equalsIgnoreCase(getUser)) {
				if (sender.hasPermission(plugin.permissionAdmin + "." + getUser.toLowerCase())) {
					if (args.length <= 1 || args.length > 2) {
						sender.sendMessage(plugin.getAPI().getChat().convertColors(
								plugin.getAPI().getChat().getMessage(NamelessMessages.INCORRECT_USAGE_GETUSER)
										.replaceAll("%command%", commandName + " " + getUser)));
					} else {
						Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
							@Override
							public void run() {
								NamelessPlayer namelessPlayer = api.getPlayer(args[0]);

								if (namelessPlayer.hasError()) {
									// Error with request
									sender.sendMessage(
											chat.convertColors("&4Error: &c" + namelessPlayer.getErrorMessage()));
								} else {

									// Display get user.
									String line = "&3&m--------------------------------";

									sender.sendMessage(chat.convertColors(line));
									sender.sendMessage(
											chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_USERNAME)
													.replaceAll("%username%", namelessPlayer.getUserName())));
									sender.sendMessage(
											chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_DISPLAYNAME)
													.replaceAll("%displayname%", namelessPlayer.getDisplayName())));
									sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_UUID)
											.replaceAll("%uuid%", namelessPlayer.getUUID())));
									sender.sendMessage(
											chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_GROUP_ID)
													.replaceAll("%groupid%", namelessPlayer.getGroupID().toString())));
									sender.sendMessage(
											chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_REGISTERED)
													.replaceAll("%registereddate%",
															namelessPlayer.getRegisteredDate().toString())));
									sender.sendMessage(chat.convertColors(
											chat.getMessage(NamelessMessages.GETUSER_REPUTATION).replaceAll(
													"%reputation%", namelessPlayer.getReputations().toString())));

									// check if validated
									if (namelessPlayer.isValidated()) {
										sender.sendMessage(chat.convertColors(
												chat.getMessage(NamelessMessages.GETUSER_VALIDATED) + "&a: "
														+ chat.getMessage(NamelessMessages.GETUSER_VALIDATED_YES)));
									} else {
										sender.sendMessage(chat.convertColors(
												chat.getMessage(NamelessMessages.GETUSER_VALIDATED) + "&c: "
														+ chat.getMessage(NamelessMessages.GETUSER_VALIDATED_NO)));
									}
									// check if banned
									if (namelessPlayer.isBanned()) {
										sender.sendMessage(chat
												.convertColors(chat.getMessage(NamelessMessages.GETUSER_BANNED) + "&c: "
														+ chat.getMessage(NamelessMessages.GETUSER_BANNED_YES)));
									} else {
										sender.sendMessage(chat
												.convertColors(chat.getMessage(NamelessMessages.GETUSER_BANNED) + "&a: "
														+ chat.getMessage(NamelessMessages.GETUSER_BANNED_NO)));
									}
									sender.sendMessage(chat.convertColors(line));
								}
							}
						});
					}
				} else {
					plugin.getAPI().getChat()
							.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION));
				}
			} else if (args[0].equalsIgnoreCase(setGroup)) {
				if (sender.hasPermission(plugin.permissionAdmin + "." + setGroup.toLowerCase())) {
					if (args.length <= 2 || args.length > 3) {
						sender.sendMessage(plugin.getAPI().getChat().convertColors(
								plugin.getAPI().getChat().getMessage(NamelessMessages.INCORRECT_USAGE_SETGROUP)
										.replaceAll("%command%", commandName + " " + setGroup)));
					} else {
						Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
							@Override
							public void run() {
								NamelessPlayer namelessPlayer = api.getPlayer(args[0]);
								Integer previousgroup = namelessPlayer.getGroupID();
								NamelessPlayerSetGroup group = namelessPlayer.setGroupID(args[1]);

								if (group.hasError()) {
									sender.sendMessage(chat.convertColors("&cError: " + group.getErrorMessage()));
								} else {
									String success = chat.getMessage(NamelessMessages.SETGROUP_SUCCESS)
											.replaceAll("%player%", group.getID())
											.replaceAll("%previousgroup%", previousgroup.toString())
											.replaceAll("%newgroup%", group.getNewGroup().toString());
									sender.sendMessage(chat.convertColors(success));
								}
							}
						});
					}
				} else {
					plugin.getAPI().getChat()
							.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION));
				}
			}
		}

		return true;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}