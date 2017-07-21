package com.namelessmc.namelessplugin.spigot.API.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.Utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.API.Utils.RequestUtil;

public class NamelessRegisterPlayer extends RequestUtil {

	public NamelessRegisterPlayer(String userName, String uuid, String email) {
		super(RequestType.Post, "register", getPostStringRegister(userName, uuid, email));
	}

	public NamelessRegisterPlayer(Player player, String email) {
		super(RequestType.Post, "register", getPostStringRegister(player.getName(), player.getUniqueId().toString(), email));

			JsonObject response = getResponse();

			if (hasError()) {
				// Error with request
				if (response.get("message").getAsString().equals("Username already exists")) {
					setErrorMessage(response.get("message").getAsString());
					player.sendMessage(NamelessChat
							.convertColors(NamelessChat.getMessage(NamelessMessages.REGISTER_USERNAME_EXISTS)));
				}
				if (response.get("message").getAsString().equals("UUID already exists")) {
					setErrorMessage(response.get("message").getAsString());
					player.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.REGISTER_UUID_EXISTS)));
				}
				if (response.get("message").getAsString().equals("Email already exists")) {
					setErrorMessage(response.get("message").getAsString());
					player.sendMessage(NamelessChat
							.convertColors(NamelessChat.getMessage(NamelessMessages.REGISTER_EMAIL_EXISTS)));
				}
			} else {
				player.sendMessage(
						NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.REGISTER_SUCCESS_MESSAGE)));
			}
	}
	
	private static String getPostStringRegister(String userName, String uuid, String email){
		String string = null;
		try {
			 string = "username=" + URLEncoder.encode(userName, "UTF-8") + "&email="
					+ URLEncoder.encode(email, "UTF-8") + "&uuid=" + URLEncoder.encode(uuid, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return string;
	}

}