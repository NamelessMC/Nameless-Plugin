package com.namelessmc.plugin.NamelessSpigot.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSpigot.Message;
import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;
import com.namelessmc.plugin.NamelessSpigot.Permission;

public class GetNotificationsCommand extends Command {

	public GetNotificationsCommand(String name) {
		super(name, Message.HELP_DESCRIPTION_GETNOTIFICATIONS.getMessage(), "/" + name);
		setPermission(Permission.COMMAND_GETNOTIFICATIONS.toString());
		setPermissionMessage(Message.NO_PERMISSION.getMessage());
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		
		if (args.length != 0) {
			sender.sendMessage(Message.INCORRECT_USAGE_GETNOTIFICATIONS.getMessage().replace("%command%", label));
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.MUST_BE_INGAME.getMessage());
			return true;
		}
		
		Player player = (Player) sender;
		
		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			NamelessPlayer nameless = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			
			if(!(nameless.exists())) {
				sender.sendMessage(Message.MUST_REGISTER.getMessage());
				return;
			}
			
			if (!(nameless.isValidated())) {
				sender.sendMessage(Message.PLAYER_NOT_VALID.getMessage());
				return;
			}
			
			int messages;
			int alerts;
			
			try {
				messages = nameless.getMessageCount();
				alerts = nameless.getAlertCount();
			} catch (NamelessException e) {
				String errorMessage = Message.NOIFICATIONS_ERROR.getMessage().replace("%error%", e.getMessage());
				player.sendMessage(errorMessage);
				return;
			}
			
			String pmMessage = Message.NOTIFICATIONS_MESSAGES.getMessage().replace("%pms%", "" + messages);
			String alertMessage = Message.NOTIFICATIONS_ALERTS.getMessage().replace("%alerts%", "" + alerts);
			String noNotifications = Message.NO_NOTIFICATIONS.getMessage();

			if (alerts == 0 && messages == 0) {
				sender.sendMessage(noNotifications);
			} else if (alerts == 0) {
				sender.sendMessage(pmMessage);
			} else if (messages == 0) {
				sender.sendMessage(alertMessage);
			} else {
				sender.sendMessage(alertMessage);
				sender.sendMessage(pmMessage);
			}
		});
		return true;
	}

}