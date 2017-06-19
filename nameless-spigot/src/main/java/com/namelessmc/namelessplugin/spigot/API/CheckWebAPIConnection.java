package com.namelessmc.namelessplugin.spigot.API;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;

public class CheckWebAPIConnection {

	NamelessPlugin plugin;

	private boolean succeeded = false;
	private boolean error = true;
	private String errorMessage;

	public CheckWebAPIConnection(NamelessPlugin plugin) {
		this.plugin = plugin;

		try {
			URL apiConnection = new URL(plugin.getAPIUrl() + "/checkConnection");

			HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", Integer.toString(0));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			// Initialise output stream
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
			// Write request
			outputStream.writeBytes("");

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

			if (response.has("success")
					|| response.get("message").getAsString().equalsIgnoreCase("Invalid API method")) {
				error = false;
				succeeded = true;
			} else if (response.has("error")) {
				System.out.println(response);
				error = true;
				succeeded = false;
				errorMessage = response.get("message").getAsString();
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();

		} catch (Exception e) {
			errorMessage = "Invalid API key";
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, "Invalid API key");
			// Exception
			e.printStackTrace();
		}
	}

	public boolean hasSucceeded() {
		return succeeded;
	}

	public boolean hasError() {
		return error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
