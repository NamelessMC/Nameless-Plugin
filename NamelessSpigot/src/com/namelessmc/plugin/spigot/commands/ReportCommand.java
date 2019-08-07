package com.namelessmc.plugin.spigot.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.Message;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;
import com.namelessmc.plugin.spigot.util.UUIDFetcher;

import xyz.derkades.derkutils.ListUtils;

public class ReportCommand extends Command {

	public ReportCommand() {
		super(Config.COMMANDS.getConfig().getString("report"), 
				Message.COMMAND_REPORT_DESCRIPTION.getMessage(), 
				Message.COMMAND_REPORT_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("report")),
				Permission.COMMAND_REPORT);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length < 2) {
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return false;
		}
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			Player player = (Player) sender;
			NamelessPlayer namelessPlayer;
			
			try {
				namelessPlayer = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
			} catch (NamelessException e) {
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
				return;
			}
			
			if (!namelessPlayer.exists()) {
				sender.sendMessage(Message.PLAYER_SELF_NOTREGISTERED.getMessage());
				return;
			}
			
			if (!namelessPlayer.isValidated()){
				player.sendMessage(Message.PLAYER_SELF_NOTVALIDATED.getMessage());
				return;
			}

			final String targetName = args[0];
			final UUID targetUuid;
			
			try {
				targetUuid = UUIDFetcher.getUUID(targetName);
			} catch (IllegalArgumentException e) {
				sender.sendMessage(Message.PLAYER_OTHER_NOTFOUND.getMessage());
				return;
			}
	
			String[] reasonWordsArray = ListUtils.removeFirstStringFromArray(args);
			String reason = String.join(" ", reasonWordsArray); //Join with space in between words
			
			try {
				namelessPlayer.createReport(targetUuid, targetName, reason);
				
				//Report successful
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_SUCCESS.getMessage());
			} catch (NamelessException e) {
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
			}
			
		});
		return true;
	}

}