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

/*
 *  Get User command
 *  By IsS127
 */
public class GetUserCommand implements CommandExecutor {
	NamelessPlugin plugin;
	String permissionAdmin;
	
	/*
	 *  Constructer
	 */
     public GetUserCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
		this.permissionAdmin =  plugin.permissionAdmin;
	}
	
	/*
	 *  Handle inputted command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender.hasPermission(permissionAdmin + ".getuser")){
			// Try to get the user
			Bukkit.getScheduler().runTaskAsynchronously(plugin,  new Runnable(){
				@Override
				public void run(){
					// Ensure username or uuid set.
					if(args.length < 1 || args.length > 1){
						sender.sendMessage(ChatColor.RED + "Incorrect usage: /getuser username/uuid");
						return;
					}
					
					// Send POST request to API
					try {
						
						// Create string containing POST contents
						String toPostStringUName = 	"username=" + URLEncoder.encode(args[0], "UTF-8");
						String toPostStringUUID = 	"uuid=" + URLEncoder.encode(args[0], "UTF-8");
						
						URL apiConnection = new URL(plugin.getAPIUrl() + "/get");
						
						HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
						connection.setRequestMethod("POST");
						// check if player typed uuid or username
						if(args[0].length() >= 17){
							connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUUID.length()));
						} else if(args[0].length() <= 16){
							connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUName.length()));
						}
						
						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						connection.setDoOutput(true);
						connection.addRequestProperty("User-Agent", 
								"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
						
						DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
						
						// Write request
						// check if player typed uuid or username
						if(args[0].length() >= 17){
							outputStream.writeBytes(toPostStringUUID);
						} else if(args[0].length() <= 16){
							outputStream.writeBytes(toPostStringUName);
						}
						
                        InputStream inputStream = connection.getInputStream();
						
						// Handle response
						BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
						StringBuilder responseBuilder = new StringBuilder();
						
						String responseString;
						while((responseString = streamReader.readLine()) != null)
							responseBuilder.append(responseString);
						JSONParser parser = new JSONParser();
						JSONObject response = new JSONObject();
						JSONObject message = new JSONObject();
						
						response = (JSONObject) parser.parse(responseBuilder.toString());
						
						// check if there isnt any error, if so parse the messages.
						if(!response.containsKey("error")){
						    message = (JSONObject) parser.parse(response.get("message").toString());
						}
						
						if(response.containsKey("error")){
							// Error with request
							sender.sendMessage(ChatColor.RED + "Error: " + response.get("message").toString());
						} else {
							
							// Convert UNIX timestamp to date
							java.util.Date registered = new java.util.Date(Long.parseLong(message.get("registered").toString()) * 1000);
							
							// Display get user.
							sender.sendMessage("§3§m--------------------------------");
							sender.sendMessage(ChatColor.GREEN + "Username: " + ChatColor.AQUA + message.get("username").toString());
							sender.sendMessage(ChatColor.GREEN + "DisplayName: " + ChatColor.AQUA + message.get("displayname").toString());
							sender.sendMessage(ChatColor.GREEN + "UUID: " + ChatColor.AQUA + message.get("uuid").toString());
							sender.sendMessage(ChatColor.GREEN + "Group ID: " + ChatColor.AQUA + message.get("group_id").toString());
							sender.sendMessage(ChatColor.GREEN + "Registered: " + ChatColor.AQUA + registered);
							sender.sendMessage(ChatColor.GREEN + "Reputation: " + ChatColor.AQUA + message.get("reputation").toString());
							
							// check if validated
							if( message.get("validated").equals("1")){
			                	sender.sendMessage(ChatColor.DARK_GREEN + "Validated: " + ChatColor.GREEN + "Yes!");
			                } else{
			                	sender.sendMessage(ChatColor.DARK_GREEN + "Validated: " + ChatColor.RED + "No!");
			                }
							// check if banned
							if( message.get("banned").equals("1")){
			                	sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.RED + "Yes!");
			                } else{
			                	sender.sendMessage(ChatColor.RED + "Banned: " + ChatColor.GREEN + "No!");
			                }
							sender.sendMessage("§3§m--------------------------------");
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
			
	}
		else{
		sender.sendMessage(ChatColor.RED + "You don't have permission to this command!");
	}
		return true;
  }	
	
}
