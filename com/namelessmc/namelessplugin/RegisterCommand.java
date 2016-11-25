package com.namelessmc.namelessplugin;

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

/*
 *  Nameless Plugin
 *  Register command
 */
public class RegisterCommand implements CommandExecutor {
	NamelessPlugin plugin;
	
	/*
	 *  Constructer
	 */
     public RegisterCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
	}
	
	/*
	 *  Handle inputted command
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3){
		// Ensure user who inputted command is player and not console
		if(sender instanceof Player){
			// Try to register user
			Bukkit.getScheduler().runTaskAsynchronously(plugin,  new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(arg3.length < 1){
						sender.sendMessage(ChatColor.RED + "Incorrect usage: /register email");
						return;
					}
					
					// Send POST request to API
					try {
						
						// Create string containing POST contents
						String toPostString = 	"username=" + URLEncoder.encode(sender.getName(), "UTF-8") +
												"&email=" + URLEncoder.encode(arg3[0], "UTF-8") + 
												"&uuid=" + URLEncoder.encode(((Player) sender).getUniqueId().toString(), "UTF-8");
						
						URL apiConnection = new URL(plugin.getAPIUrl() + "/register");
						
						HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
						connection.setRequestMethod("POST");
						connection.setRequestProperty("Content-Length", Integer.toString(toPostString.length()));
						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						connection.setDoOutput(true);
						connection.addRequestProperty("User-Agent", 
								"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
						
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
							sender.sendMessage(ChatColor.RED + "Error: " + response.get("message").getAsString());
						} else {
							// Display success message to user
							sender.sendMessage(ChatColor.GREEN + response.get("message").getAsString());
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
			
		} else {
			// User must be ingame to use register command
			sender.sendMessage("You must be ingame to use this command.");
		}
		
		return true;
	}
}
