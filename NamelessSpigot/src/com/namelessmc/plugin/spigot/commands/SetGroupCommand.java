package com.namelessmc.plugin.spigot.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.namelessmc.namelessapi.NamelessException;
import com.namelessmc.namelessapi.NamelessPlayer;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.Message;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;
import com.namelessmc.plugin.spigot.util.UUIDFetcher;

public class SetGroupCommand extends Command {

	public SetGroupCommand() {
		super(Config.COMMANDS.getConfig().getString("set-group"), 
				Message.COMMAND_SETGROUP_DESCRIPTION.getMessage(), 
				Message.COMMAND_SETGROUP_USAGE.getMessage(),
				Permission.COMMAND_SET_GROUP);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length != 2) {
			return false;
		}
		
		final int groupId;
		
		try {
			groupId = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			// The sender has provided a non numeric string
			sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_FAIL_NOTNUMERIC.getMessage());
			return true;
		}
		
		final String targetID = args[0]; // Name or UUID
		
		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {	
			NamelessPlayer target = null;
			
			try {
				// TODO Catch errors and display user friendly player not found message
				if (targetID.length() > 16) {
					//It's (probably) a UUID
					target = NamelessPlugin.getInstance().api.getPlayer(UUID.fromString(targetID));
				} else {
					//It's (probably) a name
					final UUID uuid = UUIDFetcher.getUUID(targetID);
					target = NamelessPlugin.getInstance().api.getPlayer(uuid);
				}
			} catch (NamelessException e) {
				sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_FAIL_GENERIC.getMessage());
				return;
			}
			
			if (!target.exists()) {
				sender.sendMessage(Message.PLAYER_OTHER_NOTREGISTERED.getMessage());
				return;
			}
			
			try {
				final int previousGroupId = target.getGroupID();
				target.setGroup(groupId);

				sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_SUCCESS.getMessage(
						"player", targetID,
						"old", previousGroupId,
						"new", groupId));
			} catch (NamelessException e) {
				sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
			}
		});
		return true;
	}

}