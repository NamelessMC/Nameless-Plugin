package com.namelessmc.plugin.NamelessSponge.commands;

import org.spongepowered.api.command.spec.CommandExecutor;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;

public class RegisterCommand implements CommandExecutor {

	public RegisterCommand(String name) {
		super(name, Message.HELP_DESCRIPTION_REGISTER.getMessage(), "/" + name + "<email>");
		setPermission(Permission.COMMAND_REGISTER.toString());
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		
		if (args.length != 1) {
			sender.sendMessage(Message.INCORRECT_USAGE_REGISTER.getMessage().replace("%command%", label));
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.MUST_BE_INGAME.getMessage());
			return true;
		}
		
		Player player = (Player) sender;
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			
			if (namelessPlayer.exists()) {
					sender.sendMessage(Message.HAS_REGISTERED.getMessage());
				return;
			}
			
			try {
				namelessPlayer.register(player.getName(), args[0]);
				sender.sendMessage(Message.REGISTER_SUCCESS.getMessage());
			} catch (NamelessException e) {
				if(e.getMessage().equalsIgnoreCase("Username already exists")) {
					sender.sendMessage(Message.REGISTER_EMAIL_EXISTS.getMessage());
				}else if(e.getMessage().equalsIgnoreCase("Email already exists")) {
					sender.sendMessage(Message.REGISTER_USERNAME_EXISTS.getMessage());
				}else {
					player.sendMessage(Message.REGISTER_FAIL.getMessage().replace("%error%", e.getMessage()));
					e.printStackTrace();
				}
			}
			
		});
		return true;
	}

}