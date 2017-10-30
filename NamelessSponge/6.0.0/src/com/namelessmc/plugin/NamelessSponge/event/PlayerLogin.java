package com.namelessmc.plugin.NamelessSponge.event;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSponge.Chat;
import com.namelessmc.plugin.NamelessSponge.Config;
import com.namelessmc.plugin.NamelessSponge.Message;
import com.namelessmc.plugin.NamelessSponge.NamelessPlugin;

import ninja.leaping.configurate.ConfigurationNode;

public class PlayerLogin{

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {
		Player player = event.getTargetEntity();
		
		NamelessPlugin.LOGIN_TIME.put(player.getUniqueId(), System.currentTimeMillis());

		NamelessPlugin.getGame().getScheduler().createAsyncExecutor(NamelessPlugin.getInstance()).execute(() -> {
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
			if (namelessPlayer.exists()) {
				if (namelessPlayer.isValidated()) {
					// Only show notifications if the player has validated their account
					userGetNotifications(player);
				} else {
					// If the player has not validated their account they get informed.
					player.sendMessage(Message.ACCOUNT_NOT_VALIDATED.getMessage());
				}
				
				userGroupSync(player);
			}
		});
	}

	public void userGetNotifications(Player player) {
		ConfigurationNode config = Config.MAIN.getConfig();
		if (config.getNode("join-notifications").getBoolean()) {
			try {
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				int messages = namelessPlayer.getMessageCount();
				int alerts = namelessPlayer.getAlertCount();

				Text pmMessage = Chat.toText(Message.NOTIFICATIONS_MESSAGES.getMessageAsString().replace("%pms%", messages + ""));
				Text alertMessage = Chat.toText(Message.NOTIFICATIONS_ALERTS.getMessageAsString().replace("%alerts%", alerts + ""));
				Text noNotifications = Message.NO_NOTIFICATIONS.getMessage();
				if (alerts == 0 && messages == 0) {
					player.sendMessage(noNotifications);
				} else if (alerts == 0) {
					player.sendMessage(pmMessage);
				} else if (messages == 0) {
					player.sendMessage(alertMessage);
				} else {
					player.sendMessage(alertMessage);
					player.sendMessage(pmMessage);
				}
			} catch (NamelessException e) {
				Text errorMessage = Chat.toText(Message.NOIFICATIONS_ERROR.getMessageAsString().replace("%error%", e.getMessage()));
				player.sendMessage(errorMessage);
				e.printStackTrace();
			}
		}
	}

	public void userGroupSync(Player player) {
		ConfigurationNode config = Config.MAIN.getConfig();
		if (config.getNode("group-synchronization.on-join").getBoolean()) {
			NamelessPlugin.syncGroup(player);
		}
	}

}