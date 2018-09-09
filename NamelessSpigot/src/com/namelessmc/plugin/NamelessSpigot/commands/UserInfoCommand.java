package com.namelessmc.plugin.NamelessSpigot.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Config;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;
import com.namelessmc.plugin.NamelessSpigot.util.UUIDFetcher;

public class UserInfoCommand extends Command {

	public UserInfoCommand() {
		super(Config.COMMANDS.getConfig().getString("user-info"), 
				Message.COMMAND_USERINFO_DESCRIPTION.getMessage(), 
				Message.COMMAND_USERINFO_USAGE.getMessage());
		setPermission(Permission.COMMAND_ADMIN_GETUSER.toString());
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
				return true;
			}
			
			// Player itself as first argument
			return execute(sender, label, new String[] {((Player) sender).getUniqueId().toString()});
		}
		
		if (args.length != 1) {
			return false;
		}
		
		final String targetID = args[0]; // Name or UUID

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer target;
			
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
			} catch (IllegalArgumentException e) {
				sender.sendMessage(Message.PLAYER_OTHER_NOTFOUND.getMessage());
				return;
			} catch (NamelessException e) {
				sender.sendMessage(e.getMessage());
				e.printStackTrace();
				return;
			}

			if (!target.exists()) {
				sender.sendMessage(Message.PLAYER_OTHER_NOTREGISTERED.getMessage());
				return;
			}

			//String separator = Chat.convertColors("&3&m--------------------------------");

			//sender.sendMessage(separator);
			
			String yes = Message.COMMAND_USERINFO_OUTPUT_BOOLEAN_TRUE.getMessage();
			String no = Message.COMMAND_USERINFO_OUTPUT_BOOLEAN_FALSE.getMessage();
			
			String validated = target.isValidated() ? yes : no;
			String banned = target.isBanned() ? yes : no;

			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_USERNAME.getMessage("username", target.getUsername()));
			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_DISPLAYNAME.getMessage("displayname", target.getDisplayName()));
			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_UUID.getMessage("uuid", target.getUniqueId()));
			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_GROUP.getMessage("id", target.getGroupID()));
			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_REGISTERDATE.getMessage("date", target.getRegisteredDate())); // TODO Format nicely (add option in config for date format)
			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_REPUTATION.getMessage("reputation", target.getReputation()));
			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_VALIDATED.getMessage("validated", validated));
			sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_BANNED.getMessage("banned", banned));

			//sender.sendMessage(separator);
		});
		return true;
	}

}