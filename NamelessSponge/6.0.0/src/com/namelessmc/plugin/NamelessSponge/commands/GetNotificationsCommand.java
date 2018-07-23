package com.namelessmc.plugin.NamelessSponge.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSponge.Chat;
import com.namelessmc.plugin.NamelessSponge.Message;
import com.namelessmc.plugin.NamelessSponge.NamelessPlugin;

public class GetNotificationsCommand implements CommandExecutor {

	private String label;
	
	public GetNotificationsCommand(String label) {
		this.label = label;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (args != null) {
			src.sendMessage(Chat.toText(Message.INCORRECT_USAGE_GETNOTIFICATIONS.getMessageAsString().replace("%command%", label)));
			return CommandResult.success();
		}
		
		if (!(src instanceof Player)) {
			src.sendMessage(Message.MUST_BE_INGAME.getMessage());
			return CommandResult.success();
		}
		
		Player player = (Player) src;
		
		NamelessPlugin.getGame().getScheduler().createAsyncExecutor(NamelessPlugin.getInstance()).execute((() -> {
			NamelessPlayer nameless = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			
			if(!(nameless.exists())) {
				src.sendMessage(Message.MUST_REGISTER.getMessage());
				return;
			}
			
			if (!(nameless.isValidated())) {
				src.sendMessage(Message.ACCOUNT_NOT_VALIDATED.getMessage());
				return;
			}
			
			int messages;
			int alerts;
			
			try {
				messages = nameless.getMessageCount();
				alerts = nameless.getAlertCount();
			} catch (NamelessException e) {
				Text errorMessage = Chat.toText(Message.NOTIFICATIONS_ERROR.getMessageAsString().replace("%error%", e.getMessage()));
				player.sendMessage(errorMessage);
				return;
			}
			
			Text pmMessage = Chat.toText(Message.NOTIFICATIONS_MESSAGES.getMessageAsString().replace("%pms%", "" + messages));
			Text alertMessage = Chat.toText(Message.NOTIFICATIONS_ALERTS.getMessageAsString().replace("%alerts%", "" + alerts));
			Text noNotifications = Message.NO_NOTIFICATIONS.getMessage();

			if (alerts == 0 && messages == 0) {
				src.sendMessage(noNotifications);
			} else if (alerts == 0) {
				src.sendMessage(pmMessage);
			} else if (messages == 0) {
				src.sendMessage(alertMessage);
			} else {
				src.sendMessage(alertMessage);
				src.sendMessage(pmMessage);
			}
		}));
		return CommandResult.success();
	}

}