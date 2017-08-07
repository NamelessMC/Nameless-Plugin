package com.namelessmc.plugin.NamelessSpigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;
import com.namelessmc.plugin.NamelessSpigot.commands.nameless.NamelessCommand;

/*
 *  Register CMD
 */

public class RegisterCommand extends NamelessCommand {

	/*
	 * Constructer
	 */
	public RegisterCommand(String name) {
		super(name);
		setPermission(Permission.COMMAND_REGISTER.toString());
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
		setUsage("/" + name + "<email>");
		setDescription(Message.HELP_DESCRIPTION_REGISTER.getMessage());
	}

	/*
	 * Handle inputted command
	 */
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