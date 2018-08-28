package com.namelessmc.plugin.NamelessSpigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.ApiError;
import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Chat;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;

/**
 * Command used to submit a code to validate a user's NamelessMC account
 */
public class ValidateCommand extends Command {
	
	public ValidateCommand(String name) {
		super(name, Message.HELP_DESCRIPTION_VALIDATE.getMessage(), "/" + name + "<email>");
		setPermission(Permission.COMMAND_VALIDATE.toString());
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args.length != 1) {
			sender.sendMessage(Message.INCORRECT_USAGE_VALIDATE.getMessage().replace("%command%", label));
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.MUST_BE_INGAME.getMessage());
			return true;
		}
		
		Player player = (Player) sender;
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final NamelessPlayer namelessPlayer;
			
			try {
				namelessPlayer = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
			} catch (NamelessException e) {
				sender.sendMessage(Chat.convertColors("&4An error occured, see console log for more details."));
				e.printStackTrace();
				return;
			}
			
			if (namelessPlayer.isValidated()) {
				sender.sendMessage(Message.ALREADY_VALIDATED.getMessage());
				return;
			}
			
			final String code = args[0];
			
			try {
				namelessPlayer.validate(code);
			} catch (ApiError e) {
				if (e.getErrorCode() == ApiError.INVALID_VALIDATE_CODE) {
					sender.sendMessage(Message.VALIDATION_CODE_INVALID.getMessage());
				} else {
					throw new RuntimeException(e);
				}
			} catch (NamelessException e) {
				throw new RuntimeException(e);
			}
			
		});
		
		return true;
	}

}
