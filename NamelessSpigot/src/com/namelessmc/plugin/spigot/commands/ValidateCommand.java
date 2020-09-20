package com.namelessmc.plugin.spigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.UserNotExistException;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.Message;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

/**
 * Command used to submit a code to validate a user's NamelessMC account
 */
public class ValidateCommand extends Command {

	public ValidateCommand() {
		super(Config.COMMANDS.getConfig().getString("validate"),
				Message.COMMAND_VALIDATE_DESCRIPTION.getMessage(),
				Message.COMMAND_VALIDATE_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("validate")),
				Permission.COMMAND_VALIDATE);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length != 1) {
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return true;
		}

		final Player player = (Player) sender;

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final NamelessUser user = NamelessPlugin.getInstance().api.getUser(player.getUniqueId());
				final String code = args[0];
				if (user.verifyMinecraft(code)) {
					Message.COMMAND_VALIDATE_OUTPUT_SUCCESS.send(sender);
				} else {
					Message.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE.send(sender);
				}
			} catch (final UserNotExistException e) {
				Message.PLAYER_SELF_NOTREGISTERED.send(sender);
			} catch (final NamelessException e) {
				Message.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC.send(sender);
				return;
			}
		});

		return true;
	}

}
