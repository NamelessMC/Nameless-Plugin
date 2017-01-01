package com.namelessmc.namelessplugin.bungeecord.API.Player;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NamelessRegisterPlayer {

	private boolean error;
	private boolean succeeded;
	private String errorMessage;

	public NamelessRegisterPlayer(NamelessPlugin plugin, String userName, String uuid, String email) {

		// Send POST request to API
		try {

			// Create string containing POST contents
			String toPostString = "username=" + URLEncoder.encode(userName, "UTF-8") + "&email="
					+ URLEncoder.encode(email, "UTF-8") + "&uuid=" + URLEncoder.encode(uuid, "UTF-8");

			URL apiConnection = new URL(plugin.getAPIUrl() + "/register");

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
				// Display success message to user
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
			error = true;
			errorMessage = "There was an unknown error whilst registering user.";
			succeeded = false;
			plugin.getLogger().warning(ChatColor.RED + "There was an unknown error whilst registering user.");
			// Exception
			e.printStackTrace();
		}
	}

	public NamelessRegisterPlayer(NamelessPlugin plugin, ProxiedPlayer player, String email) {
		// Send POST request to API
		try {

			// Create string containing POST contents
			String toPostString = "username=" + URLEncoder.encode(player.getName(), "UTF-8") + "&email="
					+ URLEncoder.encode(email, "UTF-8") + "&uuid="
					+ URLEncoder.encode(player.getUniqueId().toString(), "UTF-8");

			URL apiConnection = new URL(plugin.getAPIUrl() + "/register");

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
				if(response.get("message").getAsString().equals("Username already exists")){
					errorMessage = response.get("message").getAsString();
					player.sendMessage(plugin.getAPI().getChat().convertColors(
							plugin.getAPI().getChat().getMessage(NamelessMessages.REGISTER_USERNAME_EXISTS)));
				}
				if (response.get("message").getAsString().equals("UUID already exists")){
					errorMessage = response.get("message").getAsString();
					player.sendMessage(plugin.getAPI().getChat().convertColors(
							plugin.getAPI().getChat().getMessage(NamelessMessages.REGISTER_UUID_EXISTS)));
				}
				if(response.get("message").getAsString().equals("Email already exists")){
					errorMessage = response.get("message").getAsString();
					player.sendMessage(plugin.getAPI().getChat().convertColors(
							plugin.getAPI().getChat().getMessage(NamelessMessages.REGISTER_EMAIL_EXISTS)));	
				}
				
				error = true;
				succeeded = false;
			} else {
				// Display success message to user
				error = false;
				succeeded = true;
				player.sendMessage(plugin.getAPI().getChat().convertColors(
						plugin.getAPI().getChat().getMessage(NamelessMessages.REGISTER_SUCCESS_MESSAGE)));
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();

		} catch (Exception e) {
			// Exception
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
