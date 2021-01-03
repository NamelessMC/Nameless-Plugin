package com.namelessmc.spigot.commands;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.spigot.Config;
import com.namelessmc.spigot.Message;
import com.namelessmc.spigot.NamelessPlugin;
import com.namelessmc.spigot.Permission;

public class UserInfoCommand extends Command {

	public UserInfoCommand() {
		super(Config.COMMANDS.getConfig().getString("user-info"),
				Message.COMMAND_USERINFO_DESCRIPTION.getMessage(),
				Message.COMMAND_USERINFO_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("user-info")),
				Permission.COMMAND_USER_INFO);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
				return true;
			}
			
			// Player itself as first argument
			return execute(sender, new String[] { sender.getName() });
		}
		
		if (args.length != 1) {
			return false;
		}

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final Optional<NamelessUser> targetOptional;
			
			try {
				targetOptional = NamelessPlugin.getApi().getUser(args[0]);
				
				if (!targetOptional.isPresent()) {
					sender.sendMessage(Message.PLAYER_OTHER_NOTREGISTERED.getMessage());
					return;
				}
				
				final NamelessUser user = targetOptional.get();
				
				final String yes = Message.COMMAND_USERINFO_OUTPUT_YES.getMessage();
				final String no = Message.COMMAND_USERINFO_OUTPUT_NO.getMessage();
				
				final String validated = user.isVerified() ? yes : no;
				final String banned = user.isBanned() ? yes : no;
				
				sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_USERNAME.getMessage("username", user.getUsername()));
				sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_DISPLAYNAME.getMessage("displayname", user.getDisplayName()));
				sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_UUID.getMessage("uuid", user.getUniqueId().isPresent() ? user.getUniqueId().get().toString() : Message.COMMAND_USERINFO_OUTPUT_UUID_UNKNOWN.getMessage()));
				sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_GROUP.getMessage("groupname", user.getPrimaryGroup().get().getName(), "id", user.getPrimaryGroup().get().getId()));
				sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_REGISTERDATE.getMessage("date", user.getRegisteredDate())); // TODO Format nicely (add option in config for date format)
				sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_VALIDATED.getMessage("validated", validated));
				sender.sendMessage(Message.COMMAND_USERINFO_OUTPUT_BANNED.getMessage("banned", banned));
			} catch (final NamelessException e) {
				sender.sendMessage(e.getMessage());
				e.printStackTrace();
				return;
			}
		});
		return true;
	}

}