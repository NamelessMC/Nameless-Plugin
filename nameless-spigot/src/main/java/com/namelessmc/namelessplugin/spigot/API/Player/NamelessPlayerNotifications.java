package com.namelessmc.namelessplugin.spigot.API.Player;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;

public class NamelessPlayerNotifications {

	private boolean succeeded;
	private int pm = 0;
	private int alerts = 0;
	private boolean error;
	private String errorMessage;

	public NamelessPlayerNotifications(NamelessPlugin plugin, String id) {
		try {
			String toPostString = "uuid=" + URLEncoder.encode(id, "UTF-8");

			URL apiConnection = new URL(plugin.getAPIUrl() + "/getNotifications");

			HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", Integer.toString(toPostString.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			// Initialise output stream
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

			// Write request
			outputStream.writeBytes(toPostString);

			// Initialise input stream
			InputStream inputStream = connection.getInputStream();

			// Handle response
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder responseBuilder = new StringBuilder();

			String responseString;
			while ((responseString = streamReader.readLine()) != null)
				responseBuilder.append(responseString);
			JsonObject response = new JsonObject();
			JsonParser parser = new JsonParser();
			JsonObject message = new JsonObject();

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();
			message = parser.parse(response.get("message").getAsString()).getAsJsonObject();

			if (response.has("error")) {
				// Error with request
				error = true;
				succeeded = false;
				errorMessage = response.get("message").getAsString();
			} else if (response.has("error") && response.get("message").getAsString()
					.equalsIgnoreCase("Can't find user with that username or UUID!")) {
				succeeded = false;
				error = true;
				errorMessage = "Can't find user with that username or UUID!";
			} else {
				succeeded = true;
				error = false;
				alerts = message.get("alerts").getAsInt();
				pm = message.get("messages").getAsInt();
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();
		} catch (Exception e) {
			// Exception
			// Exception
			error = true;
			errorMessage = "There was an unknown error whilst executing the NamelessNotifications";
			succeeded = false;
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
					"There was an unknown error whilst executing the NamelessNotifications");
			e.printStackTrace();
		}
	}

	public Integer getPMs() {
		return pm;
	}

	public Integer getAlerts() {
		return alerts;
	}

	public boolean hasError() {
		return error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public boolean hasSucceeded() {
		return succeeded;
	}
}
