package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.commands.nameless.NamelessCommand;
import com.namelessmc.plugin.NamelessSpigot.util.UUIDFetcher;

/*
 *  GetUserCommand CMD
 */

public class GetUserCommand extends NamelessCommand {
	
	private String commandName;

	/*
	 * Constructer
	 */
	public GetUserCommand(String name) {
		super(name);
		setPermission(NamelessPlugin.PERMISSION_ADMIN + ".getuser");
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
		setUsage("/" + name + "<user>");
		setDescription(Message.HELP_DESCRIPTION_GETUSER.getMessage());

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args.length != 1) {
			sender.sendMessage(
					Message.INCORRECT_USAGE_GETUSER.getMessage().replace("%command%", commandName));
			return true;
		}
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), new Runnable() {
			@Override
			public void run() {
				final String targetName = args[0];
				
				// TODO Remove uuidfetcher for getuser cause we can use usernames only later!
				final UUID targetUuid;
				
				try {
					targetUuid = UUIDFetcher.getUUID(targetName);
				} catch (IllegalArgumentException e) {
					sender.sendMessage(Message.PLAYER_NOT_FOUND.getMessage());
					return;
				}
				
				NamelessPlayer target = new NamelessPlayer(targetUuid, NamelessPlugin.baseApiURL);
				
				if(!target.exists()) {
					sender.sendMessage(Message.PLAYER_NOT_FOUND.getMessage());
					return;
				}

				String separator = Chat.convertColors("&3&m--------------------------------");
				
				sender.sendMessage(separator);
				
				sender.sendMessage(Message.GETUSER_USERNAME.getMessage().replace("%username%", target.getUsername()));
				
				sender.sendMessage(Message.GETUSER_DISPLAYNAME.getMessage().replace("%displayname%", target.getDisplayName()));
				
				sender.sendMessage(Message.GETUSER_UUID.getMessage().replace("%uuid%", targetUuid.toString()));
				
				sender.sendMessage(Message.GETUSER_GROUP_ID.getMessage().replace("%groupid%", "" + target.getGroupID()));
		
				sender.sendMessage(Message.GETUSER_REGISTERED.getMessage().replace("%registereddate%", target.getRegisteredDate().toString()));
				
				sender.sendMessage(Message.GETUSER_REPUTATION.getMessage().replace("%reputation%", "" + target.getReputations()));

				if (target.isValidated()) {
					sender.sendMessage(Chat.convertColors(Message.GETUSER_VALIDATED.getMessage() + "&a: " + Message.GETUSER_VALIDATED_YES.getMessage()));
				} else {
					sender.sendMessage(Chat.convertColors(Message.GETUSER_VALIDATED.getMessage() + "&c: " + Message.GETUSER_VALIDATED_NO.getMessage()));
				}
				
				if (target.isBanned()) {
					sender.sendMessage(Message.GETUSER_BANNED.getMessage() +" &c: " + Message.GETUSER_BANNED_YES.getMessage());
				} else {
					sender.sendMessage(Message.GETUSER_BANNED.getMessage() + "&a: " + Message.GETUSER_BANNED_NO.getMessage());
				}
				
				sender.sendMessage(separator);
			}
		});
		return true;
	}

}