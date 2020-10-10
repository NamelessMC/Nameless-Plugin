package com.namelessmc.plugin.spigot.commands;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.Message;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

public class SetGroupCommand extends Command {

	public SetGroupCommand() {
		super(Config.COMMANDS.getConfig().getString("set-group"),
				Message.COMMAND_SETGROUP_DESCRIPTION.getMessage(),
				Message.COMMAND_SETGROUP_USAGE.getMessage(),
				Permission.COMMAND_SET_GROUP);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length != 2) {
			return false;
		}
		
		final int groupId;
		
		try {
			groupId = Integer.parseInt(args[1]);
		} catch (final NumberFormatException e) {
			// The sender has provided a non numeric string
			sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_FAIL_NOTNUMERIC.getMessage());
			return true;
		}
		
		final String targetID = args[0]; // Name or UUID
		
		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			Optional<NamelessUser> target = null;
			
			try {
				// TODO Catch errors and display user friendly player not found message
				if (targetID.length() > 16) {
					//It's (probably) a UUID
					target = NamelessPlugin.getInstance().api.getUser(UUID.fromString(targetID));
				} else {
					//It's (probably) a name
					target = NamelessPlugin.getInstance().api.getUser(targetID);
				}
			} catch (final NamelessException e) {
				sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_FAIL_GENERIC.getMessage());
				return;
			}
			
			if (!target.isPresent()) {
				sender.sendMessage(Message.PLAYER_OTHER_NOTREGISTERED.getMessage());
				return;
			}
			
			try {
				final int previousGroupId = target.get().getPrimaryGroup().getId();
				target.get().setPrimaryGroup(NamelessPlugin.getInstance().api.getGroup(groupId).get());

				sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_SUCCESS.getMessage(
						"player", targetID,
						"old", previousGroupId,
						"new", groupId));
			} catch (final NamelessException e) {
				sender.sendMessage(Message.COMMAND_SETGROUP_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
			}
		});
		return true;
	}

}