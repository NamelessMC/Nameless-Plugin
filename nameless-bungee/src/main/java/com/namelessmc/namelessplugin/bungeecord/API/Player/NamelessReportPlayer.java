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

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NamelessReportPlayer {

	private boolean error;
	private boolean succeeded;
	private String errorMessage;
	

	public NamelessReportPlayer(NamelessPlugin plugin, String reporterUUID, String[] args) {
		// Send POST request to API
		try {
			

			// Initialise strings
			String toPostReported;
			String toPostReporter;

			// Initialise JSON response + parser
			JsonObject response = new JsonObject();
			JsonParser parser = new JsonParser();

			String uuid = "";

			// Try to get the user being reported
			ProxiedPlayer reported = ProxyServer.getInstance().getPlayer(args[0]);
			if (reported == null) {
				// User is offline, get UUID from username
				HttpURLConnection lookupConnection = (HttpURLConnection) new URL(
						"https://api.mojang.com/users/profiles/minecraft/" + args[0]).openConnection();

				// Handle response
				BufferedReader streamReader = new BufferedReader(
						new InputStreamReader(lookupConnection.getInputStream(), "UTF-8"));
				StringBuilder lookupResponseBuilder = new StringBuilder();

				String lookupResponseString;
				while ((lookupResponseString = streamReader.readLine()) != null)
					lookupResponseBuilder.append(lookupResponseString);

				if (lookupResponseBuilder.toString() == null
						|| parser.parse(lookupResponseBuilder.toString()) == null) {
					error = true;
					succeeded = false;
					errorMessage = "Unable to submit report, please try again later.";
					return; // Unable to find user from username
				}

				response = (JsonObject) parser.parse(lookupResponseBuilder.toString()).getAsJsonObject();

				uuid = response.get("id").getAsString();

				if (uuid == null) {
					error = true;
					succeeded = false;
					errorMessage = "Unable to submit report, please try again later.";
					return; // Unable to find user from username
				}

				toPostReported = "reported_username=" + URLEncoder.encode(args[0], "UTF-8") + "&reported_uuid="
						+ URLEncoder.encode(uuid, "UTF-8");

			} else {
				toPostReported = "reported_username=" + URLEncoder.encode(args[0], "UTF-8") + "&reported_uuid="
						+ URLEncoder.encode(reported.getUniqueId().toString(), "UTF-8");
			}

			// Get report content
			String content = "";
			for (int i = 1; i < args.length; i++) {
				content += " " + args[i];
			}

			// Add reporter info + report content to post
			// content
			toPostReporter = "&reporter_uuid=" + URLEncoder.encode(reporterUUID, "UTF-8")
					+ "&content=" + URLEncoder.encode(content, "UTF-8");

			String toPostString = toPostReported + toPostReporter;

			// Initialise API connection
			URL apiConnection = new URL(plugin.getAPIUrl() + "/createReport");

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
			// Exception
			error = true;
			errorMessage = "There was an unknown error whilst reporting player.";
			succeeded = false;
			plugin.getLogger().warning(ChatColor.RED + "There was an unknown error whilst reporting player.");
			e.printStackTrace();
		}
	}

	public boolean hasError() {
		return error;
	}

	public boolean hasSucceeded() {
		return succeeded;
	}
	
	public String getErrorMessage(){
		return errorMessage;	
	}

}
