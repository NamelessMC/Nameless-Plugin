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
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.utils.MessagesUtil;

/*
 *  Register CMD
 */

public class RegisterCommand implements CommandExecutor {

	NamelessPlugin plugin;
	String permission;

	/*
	 *  Constructer
	 */
	public RegisterCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
		this.permission = plugin.permission;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		MessagesUtil messages = new MessagesUtil(plugin);
		// check if player has permission Permission & ensure who inputted command is a Player
		if(sender instanceof Player && sender.hasPermission(permission + ".register")){

			Player player = (Player) sender;

			// Try to register user
			Bukkit.getScheduler().runTaskAsynchronously(plugin,  new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(args.length < 1 || args.length > 1){
						player.sendMessage(ChatColor.RED + "Incorrect usage: /register email");
						return;
					}

					// Send POST request to API
					try {

						// Create string containing POST contents
						String toPostString = "username=" + URLEncoder.encode(player.getName(), "UTF-8") +
								"&email=" + URLEncoder.encode(args[0], "UTF-8") + 
								"&uuid=" + URLEncoder.encode(player.getUniqueId().toString(), "UTF-8");

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
						while((responseString = streamReader.readLine()) != null)
							responseBuilder.append(responseString);
						JsonObject response = new JsonObject();
						JsonParser parser = new JsonParser();

						response = parser.parse(responseBuilder.toString()).getAsJsonObject();

						if(response.has("error")){
							// Error with request
							player.sendMessage(ChatColor.RED + "Error: " + response.get("message").getAsString());
						} else {
							// Display success message to user
							player.sendMessage(ChatColor.GREEN + response.get("message").getAsString());
						}

						// Close output/input stream
						outputStream.flush();
						outputStream.close();
						inputStream.close();

						// Disconnect
						connection.disconnect();

					} catch(Exception e){
						// Exception
						e.printStackTrace();
					}
				}
			});

		} else if (!sender.hasPermission(permission + ".register")) {
			sender.sendMessage(messages.getMessage("NO_PERMISSION"));
		} else {
			// User must be ingame to use register command
			sender.sendMessage(ChatColor.RED + "You must be ingame to use this command.");
		}
		return true;
	}
}