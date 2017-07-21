package com.namelessmc.namelessplugin.spigot.API;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.api.config.NamelessConfigManager;
import com.namelessmc.api.player.NamelessPlayer;
import com.namelessmc.api.player.NamelessRegisterPlayer;
import com.namelessmc.api.utils.ReflectionUtil;

public class NamelessAPI {

	private NamelessConfigManager namelessConfigManager;
	private ReflectionUtil reflection;

	public NamelessPlayer getPlayer(String id) {
		NamelessPlayer player = new NamelessPlayer(id, plugin);
		return player;
	}

	public NamelessRegisterPlayer registerPlayer(String userName, String uuid, String email) {
		NamelessRegisterPlayer register = new NamelessRegisterPlayer(userName, uuid, email);
		return register;
	}

	public NamelessRegisterPlayer registerPlayer(Player player, String email) {
		NamelessRegisterPlayer register = new NamelessRegisterPlayer(player, email);
		return register;
	}

	public NamelessConfigManager getConfigManager() {
		namelessConfigManager = new NamelessConfigManager(plugin);
		return namelessConfigManager;
	}

	public ReflectionUtil getReflection() {
		return reflection;
	}
	
	private boolean succeeded = false;
	private boolean error = true;
	private String errorMessage;

	/**
	 * Checks if a web API connection can be established
	 * @return An exception if the connection was unsuccessful, null if the connection was successful.
	 */
	public static NamelessConnectException checkWebAPIConnection(URL url) {
		try {
			URL apiConnection = new URL(url + "/checkConnection");

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

			String errorMessage;
			
			if (response.has("success")
					|| response.get("message").getAsString().equalsIgnoreCase("Invalid API method")) {
				errorMessage = null;
			} else if (response.has("error")) {
				errorMessage = response.get("message").getAsString();
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();
			
			if (errorMessage == null) {
				//Error message == null - connection successful
				return null;
			} else {
				//Connection unsuccessful
				return new NamelessConnectException(errorMessage);
			}

		} catch (Exception e) {
			return new NamelessConnectException(e);
		}
	}
	
	public static class NamelessConnectException extends Exception {

		private static final long serialVersionUID = 6127505087276545949L;
		
		private String message;
		
		public NamelessConnectException(String message) {
			this.message = message;
		}
		
		public NamelessConnectException(Exception exception) {
			this.message = exception.getMessage();
		}
		
		public String getMessage() {
			return message;
		}
		
	}

}