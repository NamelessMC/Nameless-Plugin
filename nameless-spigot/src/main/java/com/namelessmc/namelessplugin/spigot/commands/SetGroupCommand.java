package com.namelessmc.namelessplugin.spigot.commands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.utils.MessagesUtil;

/*
 *  Register CMD
 */

public class SetGroupCommand implements CommandExecutor {

	NamelessPlugin plugin;
	String permissionAdmin;

	/*
	 *  Constructer
	 */
	public SetGroupCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
		this.permissionAdmin = plugin.permissionAdmin;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		MessagesUtil messages = new MessagesUtil(plugin);
		// check if player has permission Permission & ensure who inputted command is a Player
		if(sender.hasPermission(permissionAdmin + ".setgroup")){

			// Try to register user
			Bukkit.getScheduler().runTaskAsynchronously(plugin,  new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(args.length < 2 || args.length > 2){
						sender.sendMessage(ChatColor.RED + "Incorrect usage: /setgroup player groupId");
						return;
					}

					try {
						String toPostString = "username=" + URLEncoder.encode(args[0], "UTF-8") 
						+ "&group_id=" + URLEncoder.encode(args[1], "UTF-8");

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
							sender.sendMessage(ChatColor.RED + "Error: " + response.get("message").toString());
						} else {
							sender.sendMessage(ChatColor.GREEN + "Succesfully changed " + args[0] + "'s group to " + args[1]);
						}

						// Close output/input stream
						outputStream.flush();
						outputStream.close();
						inputStream.close();

						// Disconnect
						connection.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} else if (!sender.hasPermission(permissionAdmin + ".setgroup")) {
			sender.sendMessage(messages.getMessage("NO_PERMISSION"));
		}

		return true;
	}
}