package com.namelessmc.namelessplugin.spigot.API.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.spigot.API.utils.RequestUtil;

public class NamelessPlayerNotifications extends RequestUtil {

	private int pm = 0;
	private int alerts = 0;

	public NamelessPlayerNotifications(String id) {
		super(RequestType.Post, "getNotifications", getPostString(id));
		JsonObject response = getResponse();
		JsonParser parser = getParser();
		JsonObject message = new JsonObject();

		message = parser.parse(response.get("message").getAsString()).getAsJsonObject();

		if (response.has("error") && response.get("message").getAsString()
				.equalsIgnoreCase("Can't find user with that username or UUID!")) {
			setErrorMessage("Can't find user with that username or UUID!");
		} else {
			alerts = message.get("alerts").getAsInt();
			pm = message.get("messages").getAsInt();
		}
	}

	public Integer getPMs() {
		return pm;
	}

	public Integer getAlerts() {
		return alerts;
	}
}
