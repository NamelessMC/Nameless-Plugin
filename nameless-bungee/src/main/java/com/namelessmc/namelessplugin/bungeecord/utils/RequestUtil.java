package com.namelessmc.namelessplugin.bungeecord.utils;

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
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class RequestUtil {

	NamelessPlugin plugin;

	public RequestUtil(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public void setGroup(String playerName, String groupId) throws Exception{
		String toPostString = "username=" + URLEncoder.encode(playerName, "UTF-8") 
			+ "&group_id=" + URLEncoder.encode(groupId, "UTF-8");

		URL apiConnection = new URL(plugin.getAPIUrl() + "/setGroup");

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
		while((responseString = streamReader.readLine()) != null)
			responseBuilder.append(responseString);
		JsonObject response = new JsonObject();
		JsonParser parser = new JsonParser();

		response = parser.parse(responseBuilder.toString()).getAsJsonObject();

		if(response.has("error")){
			// Error with request
			plugin.getLogger().info(ChatColor.RED + "Error: " + response.get("message").getAsString());
		}

		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();
	}

	public String getGroup(String playerName) throws Exception{
		String toPostString = "username=" + URLEncoder.encode(playerName, "UTF-8");

		URL apiConnection = new URL(plugin.getAPIUrl() + "/get");

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

        InputStream inputStream = connection.getInputStream();
		
		// Handle response
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		StringBuilder responseBuilder = new StringBuilder();
		
		String responseString;
		while((responseString = streamReader.readLine()) != null)
			responseBuilder.append(responseString);
		JsonParser parser = new JsonParser();
		JsonObject response = new JsonObject();
		JsonObject message = new JsonObject();
		
		response = (JsonObject) parser.parse(responseBuilder.toString());

		if(response.has("error")){
			// Error with request
			plugin.getLogger().info(ChatColor.RED + "Error: " + response.get("message").toString());
		}

		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();

		return message.get("group_id").toString();
	}

	public void getNotifications(ProxiedPlayer player) throws Exception{
		String toPostString = "uuid=" + URLEncoder.encode(player.getUniqueId().toString().replace("-", ""), "UTF-8");

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
		while((responseString = streamReader.readLine()) != null)
			responseBuilder.append(responseString);
		JsonObject response = new JsonObject();
		JsonParser parser = new JsonParser();
		JsonObject message = new JsonObject();

		response = parser.parse(responseBuilder.toString()).getAsJsonObject();

		if(response.has("error")){
			// Error with request
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Error: " + response.get("message").getAsString()));
		} else if(response.has("error") && response.getAsString().equalsIgnoreCase("Can't find user with that UUID!")){
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You must register to get notifications."));
		} else if(message.get("alerts").toString().equals("0") && message.get("messages").toString().equals("0")){
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "Alerts: " + ChatColor.RED + "None"));
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "PMs: " + ChatColor.RED + "None"));
		} else if(message.get("alerts").toString().equals("0")){
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "Alerts: " + ChatColor.RED + "None"));
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "PMs: " + ChatColor.GREEN + message.get("messages").toString()));
		} else if(message.get("messages").toString().equals("0")){
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "Alerts: " + ChatColor.GREEN + message.get("alerts").toString()));
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "PMs: " + ChatColor.RED + "None"));
		} else {
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "Alerts: " + ChatColor.GREEN + message.get("alerts").toString()));
			player.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "PMs: " + ChatColor.GREEN + message.get("messages").toString()));
		}

		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();
	}

}