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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;

/*
 *  Nameless Plugin
 *  Report command
 */
public class ReportCommand implements CommandExecutor {
	
	NamelessPlugin plugin;
	String permission;
	
	/*
	 *  Constructor
	 */
     public ReportCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
		this.permission =  plugin.permission;
	}
	
	/*
	 *  Handle inputted command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender.hasPermission(permission + ".report")){
			// Ensure user who inputted command is player and not console
			if(sender instanceof Player){
				Player player = (Player) sender;
			
				// Try to get the user
				Bukkit.getScheduler().runTaskAsynchronously(plugin,  new Runnable(){
					@Override
					public void run(){
						// Ensure username or uuid set.
						if(args.length < 2){
							sender.sendMessage(ChatColor.RED + "Incorrect usage: /report username reason");
							return;
						}
						
						// Send POST request to API
						try {
							
							// Initialise strings
							String toPostReported;
							String toPostReporter;
							
							// Get UUID of reported player
							Player reported = Bukkit.getPlayerExact(args[0]);
							if(reported == null){
								// Get information about offline player
								@SuppressWarnings("deprecation")
								OfflinePlayer offline = Bukkit.getServer().getOfflinePlayer(args[0]);
								if(offline == null)
									return; // Invalid username
								else {
									toPostReported =  "reported_username=" + URLEncoder.encode(args[0], "UTF-8")
													+ "&reported_uuid=" + URLEncoder.encode(offline.getUniqueId().toString(), "UTF-8");
								}
							} else {
								toPostReported =  "reported_username=" + URLEncoder.encode(args[0], "UTF-8")
												+ "&reported_uuid=" + URLEncoder.encode(reported.getUniqueId().toString(), "UTF-8");
							}
							
							// Get report content
							String content = "";
							for (int i = 1; i < args.length; i++) {
								content += " " + args[i];
							}
							
							// Add reporter info + report content to post content
							toPostReporter =  "&reporter_uuid=" + URLEncoder.encode(player.getUniqueId().toString(), "UTF-8")
										   	+ "&content=" + URLEncoder.encode(content, "UTF-8");
							
							String toPostString = toPostReported + toPostReporter;
							
							// Initialise API connection
							URL apiConnection = new URL(plugin.getAPIUrl() + "/createReport");
							
							HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
							connection.setRequestMethod("POST");
							
							connection.setRequestProperty("Content-Length", Integer.toString(toPostString.length()));
							
							connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
							connection.setDoOutput(true);
							connection.addRequestProperty("User-Agent", 
									"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
							
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
							
							// Check if there are any errors
							if(response.containsKey("error")){
								// Error with request
								sender.sendMessage(ChatColor.RED + "Error: " + response.get("message").toString());
							} else {
								// Success
								sender.sendMessage(ChatColor.GREEN + response.get("message").toString());
							}
							
							// Close output/input stream
							outputStream.flush();
							outputStream.close();
							inputStream.close();
							
							// Disconnect
							connection.disconnect();
							
						} catch(Exception e){
							// Exception
							sender.sendMessage(ChatColor.RED + "There was an unknown error whilst executing the command.");
							e.printStackTrace();
						}
					}
				});
			} else 
				sender.sendMessage("You must be ingame to use this command.");
		}
		else
			sender.sendMessage(ChatColor.RED + "You don't have permission to this command!");
		
		return true;
  }	
	
}
