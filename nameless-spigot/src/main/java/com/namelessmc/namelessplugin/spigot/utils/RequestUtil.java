package com.namelessmc.namelessplugin.spigot.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

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
		JSONObject response = new JSONObject();
		JSONParser parser = new JSONParser();

		response = (JSONObject) parser.parse(responseBuilder.toString());

		if(response.containsKey("error")){
			// Error with request
			Bukkit.getLogger().info(ChatColor.RED + "Error: " + response.get("message").toString());
		} else {
			Bukkit.getLogger().info(ChatColor.GREEN + "Succesfully changed " + playerName + "'s group to " + groupId);
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
		JSONParser parser = new JSONParser();
		JSONObject response = new JSONObject();
		
		response = (JSONObject) parser.parse(responseBuilder.toString());
		JSONObject message = (JSONObject) parser.parse(response.get("message").toString());
		
		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();

		if(response.containsKey("error")){
			return null;
		} else {
			return message.get("group_id").toString();
		}
	}
	
	public String getUserName(String uuid) throws Exception{
		String toPostString = "uuid=" + URLEncoder.encode(uuid, "UTF-8");

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
		JSONParser parser = new JSONParser();
		JSONObject response = new JSONObject();
		
		response = (JSONObject) parser.parse(responseBuilder.toString());
		JSONObject message = (JSONObject) parser.parse(response.get("message").toString());
		
		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();

		if(response.containsKey("error")){
			plugin.getLogger().info(ChatColor.RED + "Error: " + response.get("message").toString());
			return null;
		} else {
			return message.get("username").toString();
		}
	}
	
	public void updateUserName(String uuid, String newUsername) throws Exception{
		String toPostString = "id=" + URLEncoder.encode(uuid, "UTF-8") 
			+ "&new_username=" + URLEncoder.encode(newUsername, "UTF-8");

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
		while((responseString = streamReader.readLine()) != null)
			responseBuilder.append(responseString);
		JSONObject response = new JSONObject();
		JSONParser parser = new JSONParser();

		response = (JSONObject) parser.parse(responseBuilder.toString());

		if(response.containsKey("error")){
			// Error with request
			Bukkit.getLogger().info(ChatColor.RED + "Error: " + response.get("message").toString());
		} else {
			Bukkit.getLogger().info(ChatColor.GREEN + "Succesfully changed " + uuid + "'s username to " + newUsername);
		}

		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();
	}

	public void getNotifications(Player player) throws Exception{
		String toPostString = "uuid=" + URLEncoder.encode(player.getName(), "UTF-8");

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
		JSONObject response = new JSONObject();
		JSONParser parser = new JSONParser();
		
		response = (JSONObject) parser.parse(responseBuilder.toString());
		JSONObject message = (JSONObject) parser.parse(response.get("message").toString());
		
		if(response.containsKey("error")){
			// Error with request
			player.sendMessage(ChatColor.RED + "Error: " + response.get("message"));
		} else if(response.containsKey("error") && response.get("message").toString().equalsIgnoreCase("Can't find user with that username or UUID!")){
			player.sendMessage(ChatColor.RED + "You must register to get notifications.");
		} else if(message.get("alerts").toString().equals("0") && message.get("messages").toString().equals("0")){
			player.sendMessage(ChatColor.RED + "You have no notifications.");
		} else if(message.get("alerts").toString().equals("0")){
			player.sendMessage(ChatColor.GOLD + "PMs: " + ChatColor.GREEN + message.get("messages").toString());
		} else if(message.get("messages").toString().equals("0")){
			player.sendMessage(ChatColor.GOLD + "Alerts: " + ChatColor.GREEN + message.get("alerts").toString());
		} else {
			player.sendMessage(ChatColor.GOLD + "Alerts: " + ChatColor.GREEN + message.get("alerts").toString());
			player.sendMessage(ChatColor.GOLD + "PMs: " + ChatColor.GREEN + message.get("messages").toString());
		}

		// Close output/input stream
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		// Disconnect
		connection.disconnect();
	}

}
