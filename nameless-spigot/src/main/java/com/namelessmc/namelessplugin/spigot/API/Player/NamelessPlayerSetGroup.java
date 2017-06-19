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

public class NamelessPlayerSetGroup {

	NamelessPlugin plugin;

	private String id;
	private boolean error;
	private boolean succeeded;
	private String errorMessage;

	private Integer newGroup;

	public NamelessPlayerSetGroup(NamelessPlugin plugin, String id, Integer newGroup) {
		this.plugin = plugin;
		this.id = id;
		this.newGroup = newGroup;

		try {

			// Create string containing POST contents
			String toPostStringUName = "username=" + URLEncoder.encode(id, "UTF-8") + "&group_id="
					+ URLEncoder.encode(newGroup.toString(), "UTF-8");
			String toPostStringUUID = "uuid=" + URLEncoder.encode(id, "UTF-8") + "&group_id="
					+ URLEncoder.encode(newGroup.toString(), "UTF-8");

			URL apiConnection = new URL(plugin.getAPIUrl() + "/setGroup");

			HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
			connection.setRequestMethod("POST");

			// check if player typed uuid or username
			if (id.length() >= 17) {
				connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUUID.length()));
			} else if (id.length() <= 16) {
				connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUName.length()));
			}

			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

			// Write request
			// check if player typed uuid or username
			if (id.length() >= 17) {
				outputStream.writeBytes(toPostStringUUID);
			} else if (id.length() <= 16) {
				outputStream.writeBytes(toPostStringUName);
			}

			InputStream inputStream = connection.getInputStream();

			// Handle response
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder responseBuilder = new StringBuilder();

			String responseString;
			while ((responseString = streamReader.readLine()) != null)
				responseBuilder.append(responseString);

			JsonParser parser = new JsonParser();
			JsonObject response = new JsonObject();

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
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, "There was an unknown error whilst setting group");
			e.printStackTrace();
		}
	}

	public NamelessPlayerSetGroup(NamelessPlugin plugin, String id, String newGroup) {
		this.plugin = plugin;
		this.id = id;

		try {

			// Create string containing POST contents
			String toPostStringUName = "username=" + URLEncoder.encode(id, "UTF-8") + "&group_id="
					+ URLEncoder.encode(newGroup, "UTF-8");
			String toPostStringUUID = "uuid=" + URLEncoder.encode(id, "UTF-8") + "&group_id="
					+ URLEncoder.encode(newGroup, "UTF-8");

			URL apiConnection = new URL(plugin.getAPIUrl() + "/setGroup");

			HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
			connection.setRequestMethod("POST");

			// check if player typed uuid or username
			if (id.length() >= 17) {
				connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUUID.length()));
			} else if (id.length() <= 16) {
				connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUName.length()));
			}

			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

			// Write request
			// check if player typed uuid or username
			if (id.length() >= 17) {
				outputStream.writeBytes(toPostStringUUID);
			} else if (id.length() <= 16) {
				outputStream.writeBytes(toPostStringUName);
			}

			InputStream inputStream = connection.getInputStream();

			// Handle response
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder responseBuilder = new StringBuilder();

			String responseString;
			while ((responseString = streamReader.readLine()) != null)
				responseBuilder.append(responseString);

			JsonParser parser = new JsonParser();
			JsonObject response = new JsonObject();

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();

			if (response.has("error")) {
				// Error with request
				error = true;
				succeeded = false;
				errorMessage = response.get("message").getAsString();
			} else {
				this.newGroup = plugin.getAPI().getPlayer(id).getGroupID();

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
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, "There was an unknown error whilst setting group");
			e.printStackTrace();
		}
	}

	public boolean hasError() {
		return error;
	}

	public boolean hasSucceseded() {
		return succeeded;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getID() {
		return id;
	}

	public Integer getNewGroup() {
		return newGroup;
	}
}
