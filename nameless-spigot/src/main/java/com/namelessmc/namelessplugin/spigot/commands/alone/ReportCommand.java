package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessReportPlayer;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  Report CMD
 */

public class ReportCommand extends NamelessCommand{

	NamelessPlugin plugin;
	String commandName;

	/*
	 * Constructer
	 */
	public ReportCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(plugin.permission + ".report");
		setPermissionMessage(plugin.getAPI().getChat()
				.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION)));
		usageMessage = "/" + name + "<user> <report text>";
		
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

			// Try to report
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					NamelessAPI api = plugin.getAPI();
					NamelessChat chat = api.getChat();

					// Ensure email is set
					if (args.length < 2) {
						player.sendMessage(
								chat.convertColors(chat.getMessage(NamelessMessages.INCORRECT_USAGE_REPORT).replaceAll("%command%", commandName)));
						return;
					} else {
						NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
						NamelessReportPlayer report = namelessPlayer.reportPlayer(args);

						if (report.hasError()) {
							// Error with request
							player.sendMessage(chat.convertColors("&4Error: " + report.getErrorMessage()));
						} else {
							// Display success message to user
							player.sendMessage(chat.convertColors(
									chat.getMessage(NamelessMessages.REPORT_SUCCESS).replaceAll("%player%", args[0])));
						}
					}
				}
			});

		} else {
			NamelessAPI api = plugin.getAPI();
			NamelessChat chat = api.getChat();
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
		return true;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}