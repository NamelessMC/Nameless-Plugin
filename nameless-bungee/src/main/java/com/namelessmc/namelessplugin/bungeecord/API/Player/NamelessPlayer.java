package com.namelessmc.namelessplugin.bungeecord.API.Player;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;

public class NamelessPlayer {

	private NamelessPlugin plugin;
	private String id;

	private String userName;
	private String displayName;
	private String uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean error;
	private boolean validated;
	private boolean banned;
	private String errorMessage;

	public NamelessPlayer(String id, NamelessPlugin plugin) {
		// Reigster Plugin
		this.plugin = plugin;
		this.id = id;

		// Send POST request to API
		try {

			// Create string containing POST contents
			String toPostStringUName = "username=" + URLEncoder.encode(id, "UTF-8");
			String toPostStringUUID = "uuid=" + URLEncoder.encode(id, "UTF-8");

			URL apiConnection = new URL(plugin.getAPIUrl() + "/get");

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
			JsonObject message = new JsonObject();

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();

			// check if there isnt any error, if so parse the messages.
			if (!response.has("error")) {
				message = parser.parse(response.get("message").getAsString()).getAsJsonObject();
			}

			if (response.has("error")) {
				// Error with request
				exists = false;
				error = true;
				errorMessage = response.get("message").getAsString();
			} else {

				exists = true;
				error = false;

				// Convert UNIX timestamp to date
				java.util.Date registered = new java.util.Date(
						Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

				// Display get user.
				userName = message.get("username").getAsString();
				displayName = message.get("displayname").getAsString();
				uuid = message.get("uuid").getAsString();
				groupID = message.get("group_id").getAsInt();
				registeredDate = registered;
				reputation = message.get("reputation").getAsInt();

				// check if validated
				if (message.get("validated").getAsString().equals("1")) {
					validated = true;
				} else {
					validated = false;
				}
				// check if banned
				if (message.get("banned").getAsString().equals("1")) {
					banned = true;
				} else {
					banned = false;
				}

			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();

		} catch (Exception e) {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, "There was an unknown error whilst getting player.");
			e.printStackTrace();
		}
	}

	public String getUserName() {
		return userName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUUID() {
		return uuid;
	}

	public Integer getGroupID() {
		return groupID;
	}

	public NamelessPlayerNotifications getNotifications() {
		NamelessPlayerNotifications notificaitons = new NamelessPlayerNotifications(plugin, id);
		return notificaitons;
	}

	public Integer getReputations() {
		return reputation;
	}

	public Date getRegisteredDate() {
		return registeredDate;
	}

	// Checks if player exists or not.
	public boolean exists() {
		return exists;
	}

	// Gets if player has validated or not.
	public boolean hasError() {
		return error;
	}

	public String getErrorMessage(){
		return errorMessage;
	}
	public boolean isValidated() {
		return validated;
	}

	public boolean isBanned() {
		return banned;
	}

	public NamelessPlayerSetGroup setGroupID(int newGroup) {
		NamelessPlayerSetGroup setGroup = new NamelessPlayerSetGroup(plugin, id, newGroup);
		return setGroup;
	}

	public NamelessPlayerSetGroup setGroupID(String newGroup) {
		NamelessPlayerSetGroup setGroup = new NamelessPlayerSetGroup(plugin, id, newGroup);
		return setGroup;
	}

	public NamelessPlayerUpdateUsername updateUsername(String newUserName) {
		NamelessPlayerUpdateUsername updateUsername = new NamelessPlayerUpdateUsername(plugin, id, newUserName);
		return updateUsername;
	}
	
	public NamelessReportPlayer reportPlayer(String[] args){
		NamelessReportPlayer report = new NamelessReportPlayer(plugin, uuid, args);
		return report;
	}

}
