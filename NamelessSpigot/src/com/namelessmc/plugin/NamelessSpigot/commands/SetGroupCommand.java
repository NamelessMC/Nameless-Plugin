package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;
import com.namelessmc.plugin.NamelessSpigot.util.UUIDFetcher;

public class SetGroupCommand extends Command {

	public SetGroupCommand(String name) {
		super(name, Message.HELP_DESCRIPTION_SETGROUP.getMessage(), "/" + name + "<user> <groupID>");
		setPermission(Permission.COMMAND_ADMIN_SETGROUP.toString());
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args.length != 2) {
			sender.sendMessage(Message.INCORRECT_USAGE_SETGROUP.getMessage().replace("%command%", label));
			return false;
		}
		
		final int groupId;
		
		try {
			groupId = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			// The sender has provided a non numeric string
			sender.sendMessage(Message.INCORRECT_USAGE_SETGROUP.getMessage().replaceAll("%command%", label));
			return false;
		}
		
		
		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final String targetID = args[0]; // Name or UUID
			
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
				sender.sendMessage(Chat.convertColors("&4An error occured, see console log for more details."));
				e.printStackTrace();
				return;
			}
			
			if(!target.exists()) {
				sender.sendMessage(Message.PLAYER_NOT_FOUND.getMessage());
				return;
			}
			
			try {
				final int previousGroupId = target.getGroupID();
				target.setGroup(groupId);
			
				String success = Message.SETGROUP_SUCCESS.getMessage()
						.replace("%player%", args[1])
						.replace("%previousgroup%", String.valueOf(previousGroupId))
						.replace("%newgroup%", String.valueOf(groupId));
				sender.sendMessage(success);
			} catch (NamelessException e) {
				sender.sendMessage(Message.SETGROUP_ERROR.getMessage().replace("%error%", e.getMessage()));
				e.printStackTrace();
			}
		});
		return true;
	}

}