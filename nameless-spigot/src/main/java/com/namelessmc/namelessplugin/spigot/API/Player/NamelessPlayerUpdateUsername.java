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

public class NamelessPlayerUpdateUsername {

	NamelessPlugin plugin;

	private boolean error;
	private boolean succeeded;
	private String errorMessage;

	public NamelessPlayerUpdateUsername(NamelessPlugin plugin, String id, String newUsername) {
		this.plugin = plugin;

		try {

			String toPostString = "id=" + URLEncoder.encode(id, "UTF-8") + "&new_username="
					+ URLEncoder.encode(newUsername, "UTF-8");

			URL apiConnection = new URL(plugin.getAPIUrl() + "/updateUsername");

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

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();

			if (response.has("error")) {
				// Error with request
				error = true;
				succeeded = false;
				errorMessage = response.get("message").getAsString();
			} else {
				error = false;
				succeeded = true;
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();
		} catch (Exception e) {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
					"There was an unknown error whilst updating username");
			e.printStackTrace();
		}
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
}
