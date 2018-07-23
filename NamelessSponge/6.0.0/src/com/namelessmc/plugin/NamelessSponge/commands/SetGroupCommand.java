package com.namelessmc.plugin.NamelessSponge.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.spongepowered.api.command.spec.CommandExecutor;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;

public class SetGroupCommand implements CommandExecutor {

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
		
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final String targetID = args[0];
			
			NamelessPlayer target = null;
			
			if(targetID.length() > 16) {
				target = new NamelessPlayer(UUID.fromString(targetID), NamelessPlugin.baseApiURL);
			}else {
				target = new NamelessPlayer(targetID, NamelessPlugin.baseApiURL);
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