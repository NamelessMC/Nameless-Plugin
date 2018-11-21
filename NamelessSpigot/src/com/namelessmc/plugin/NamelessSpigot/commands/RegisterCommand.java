package com.namelessmc.plugin.NamelessSpigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.ApiError;
import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Config;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;

public class RegisterCommand extends Command {

	public RegisterCommand() {
		super(Config.COMMANDS.getConfig().getString("register"), 
				Message.COMMAND_REGISTER_DESCRIPTION.getMessage(), 
				Message.COMMAND_REGISTER_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("register")),
				Permission.COMMAND_REGISTER);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length != 1) {
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return true;
		}
		
		Player player = (Player) sender;
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer;
			
			try {
				namelessPlayer = NamelessPlugin.getInstance().api.getPlayer(player.getUniqueId());
			} catch (NamelessException e) {
				sender.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
				return;
			}
			
			if (namelessPlayer.exists()) {
				sender.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS.getMessage());
				return;
			}
			
			try {
				String link = namelessPlayer.register(player.getName(), args[0]);
				if (link.equals("")) {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL.getMessage());
				} else {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_SUCCESS_LINK.getMessage("link", link));
				}
			} catch (ApiError e) {
				if (e.getErrorCode() == ApiError.EMAIL_ALREADY_EXISTS) {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED.getMessage());
				} else {
					player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
					e.printStackTrace();
				}
			} catch (NamelessException e) {
				player.sendMessage(Message.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
			}
			
		});
		
		return true;
	}

}