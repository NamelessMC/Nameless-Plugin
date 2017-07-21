package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessReportPlayer;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  Report CMD
 */

public class ReportCommand extends NamelessCommand {

	private NamelessPlugin plugin;
	private String commandName;

	/*
	 * Constructer
	 */
	public ReportCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(NamelessPlugin.permission + ".report");
		setPermissionMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
		setUsage("/" + name + "<user> <report text>");
		setDescription(NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REPORT));

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		// Instance is a Player
		if (sender instanceof Player) {

			Player player = (Player) sender;
			NamelessAPI api = plugin.getAPI();
			NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
			if (namelessPlayer.exists()) {
				if (namelessPlayer.isValidated()) {
					// Try to report
					Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
						@Override
						public void run() {

							// Ensure email is set
							if (args.length < 2) {
								player.sendMessage(NamelessChat
										.convertColors(NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_REPORT)
												.replaceAll("%command%", commandName)));
								return;
							} else {
								NamelessReportPlayer report = namelessPlayer.reportPlayer(args);

								if (report.hasError()) {
									// Error with request
									player.sendMessage(
											NamelessChat.convertColors("&4Error: " + report.getErrorMessage()));
								} else {
									// Display success message to user
									player.sendMessage(NamelessChat
											.convertColors(NamelessChat.getMessage(NamelessMessages.REPORT_SUCCESS)
													.replaceAll("%player%", args[0])));
								}
							}
						}
					});
				} else {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.PLAYER_NOT_VALID)));
				}
			} else {
				sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_REGISTER)));
			}

		} else {
			sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
		return true;
	}

}