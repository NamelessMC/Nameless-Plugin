package com.namelessmc.spigot.commands;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.CannotSendEmailException;
import com.namelessmc.java_api.exception.InvalidUsernameException;
import com.namelessmc.spigot.Config;
import com.namelessmc.spigot.Message;
import com.namelessmc.spigot.NamelessPlugin;
import com.namelessmc.spigot.Permission;

public class RegisterCommand extends Command {

	public RegisterCommand() {
		super(Config.COMMANDS.getConfig().getString("register"),
				Message.COMMAND_REGISTER_DESCRIPTION.getMessage(),
				Message.COMMAND_REGISTER_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("register")),
				Permission.COMMAND_REGISTER);
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
				final Optional<String> link = NamelessPlugin.getApi().registerUser(player.getName(), args[0], Optional.of(player.getUniqueId()));
				if (link.isPresent()) {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_SUCCESS_LINK.getMessage("link", link.get()));
				} else {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL.getMessage());
				}
			} catch (final ApiError e) {
				if (e.getError() == ApiError.EMAIL_ALREADY_EXISTS) {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED.getMessage());
				} else if (e.getError() == ApiError.USERNAME_ALREADY_EXISTS) {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS.getMessage());
				} else if (e.getError() == ApiError.INVALID_EMAIL_ADDRESS) {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID.getMessage());
				} else {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
					e.printStackTrace();
				}
			} catch (final NamelessException e) {
				player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
			} catch (final InvalidUsernameException e) {
				player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_USERNAMEINVALID.getMessage());
			} catch (final CannotSendEmailException e) {
				player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_CANNOTSENDEMAIL.getMessage());
			}
		});
		
		return true;
	}

}