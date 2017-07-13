package com.namelessmc.namelessplugin.spigot.API.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MojangPlayerRequest {

	private JsonParser parser = new JsonParser();
	private JsonObject response = new JsonObject();

	private boolean error;
	private boolean succeeded;
	private String errorMessage;

	private String uuid;

	public MojangPlayerRequest(String player) {
		try {
			// User is offline, get UUID from username
			HttpsURLConnection lookupConnection = (HttpsURLConnection) new URL(
					"https://api.mojang.com/users/profiles/minecraft/" + player).openConnection();

			// Handle response
			BufferedReader streamReader = new BufferedReader(
					new InputStreamReader(lookupConnection.getInputStream(), "UTF-8"));
			StringBuilder lookupResponseBuilder = new StringBuilder();

			String lookupResponseString;
			while ((lookupResponseString = streamReader.readLine()) != null)
				lookupResponseBuilder.append(lookupResponseString);

			if (lookupResponseBuilder.toString() == null || parser.parse(lookupResponseBuilder.toString()) == null) {
				error = true;
				succeeded = false;
				errorMessage = "Unable to submit report, please try again later.";
				return; // Unable to find user from username
			}

			response = getParser().parse(lookupResponseBuilder.toString()).getAsJsonObject();

			uuid = getResponse().get("id").getAsString();

			if (uuid == null) {
				error = true;
				succeeded = false;
				errorMessage = "Unable to submit report, please try again later.";
				return; // Unable to find user from username
			}
		} catch (Exception e) {
			// Exception
			error = true;
			errorMessage = "There was an unknown error whilst reporting player.";
			succeeded = false;
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
					"&4There was an unknown error whilst reporting player.");
			e.printStackTrace();
		}
	}

	public JsonObject getResponse() {
		return response;
	}

	public JsonParser getParser() {
		return parser;
	}

	public boolean hasError() {
		return error;
	}

	public boolean hasSucceeded() {
		return succeeded;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getUUID() {
		return uuid;
	}
}
