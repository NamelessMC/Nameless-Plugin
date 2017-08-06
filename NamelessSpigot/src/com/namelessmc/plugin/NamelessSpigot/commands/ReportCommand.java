package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.util.UUIDFetcher;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.commands.nameless.NamelessCommand;

/*
 *  Report CMD
 */

public class ReportCommand extends NamelessCommand {

	/*
	 * Constructer
	 */
	public ReportCommand(String name) {
		super(name);
		setPermission(NamelessPlugin.PERMISSION + ".report");
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
		setUsage("/" + name + "<user> <report text>");
		setDescription(Message.HELP_DESCRIPTION_REPORT.getMessage());
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.MUST_BE_INGAME.getMessage());
			return false;
		}
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			
			Player player = (Player) sender;
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			
			if (namelessPlayer.exists()) {
				sender.sendMessage(Message.MUST_REGISTER.getMessage());
			}
			
			if (namelessPlayer.isValidated()){
				player.sendMessage(Message.PLAYER_NOT_VALID.getMessage());
				return;
			}
						
			if (args.length < 2) {
				player.sendMessage(
						Message.INCORRECT_USAGE_REPORT.getMessage().replace("%command%", label));
				return;
			}
			
			final String targetName = args[0];
			final UUID targetUuid;
			
			try {
				targetUuid = UUIDFetcher.getUUID(targetName);
			} catch (IllegalArgumentException e) {
				sender.sendMessage(Message.PLAYER_NOT_FOUND.getMessage());
				return;
			}
			
			//Remove first argument, all other arguments are part of the reason
			int n = args.length - 1;
			String[] reasonWordsArray = new String[n];
			System.arraycopy(args, 1, reasonWordsArray, 0, n);
			
			String reason = String.join(" ", reasonWordsArray); //Join with space in between words
			
			try {
				namelessPlayer.reportPlayer(targetUuid, targetName, reason);
				
				//Report successful
				sender.sendMessage(
						Message.REPORT_SUCCESS.getMessage().replace("%player%", args[0]));
			} catch (NamelessException e) {
				sender.sendMessage(Message.REPORT_ERROR.getMessage().replace("%error%", e.getMessage()));
			}
			
		});
		return true;
	}

}