package com.namelessmc.namelessplugin.bungeecord.commands;

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
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

    /*
     * GetUser command made by IsS127
     */

public class GetUserCommand extends Command {

	NamelessPlugin plugin;
	String permissionAdmin;
	
	/*
	 *  Constructer
	 */
	public GetUserCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permissionAdmin = pluginInstance.permissionAdmin;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permissionAdmin Permission
		if(sender.hasPermission(permissionAdmin + ".getuser")){
			// check if has set url.
			if(plugin.hasSetUrl == false){
				sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Please set a API Url in the configuration!"));
				return;
			}

			// Try to get the user
			ProxyServer.getInstance().getScheduler().runAsync(plugin,  new Runnable(){
				@Override
				public void run(){
					// Ensure username or uuid set.
					if(args.length < 1 || args.length > 1){
						new TextComponent();
						sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Incorrect usage: /getuser username/uuid"));
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
						connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

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

						JsonParser parser = new JsonParser();
						JsonObject response = new JsonObject();
						JsonObject message = new JsonObject();

						response = parser.parse(responseBuilder.toString()).getAsJsonObject();

						// check if there isnt any error, if so parse the messages.
						if(!response.has("error")){
							message = parser.parse(response.get("message").getAsString()).getAsJsonObject();
						}

						if(response.has("error")){
							// Error with request
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Error: " + response.get("message").toString()));
						} else {

							// Convert UNIX timestamp to date
							java.util.Date registered = new java.util.Date(Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

							// Display get user.
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&3&m--------------------------------")));
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Username: " + ChatColor.AQUA + message.get("username").getAsString()));
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "DisplayName: " + ChatColor.AQUA + message.get("displayname").getAsString()));
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "UUID: " + ChatColor.AQUA + message.get("uuid").getAsString()));
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Group ID: " + ChatColor.AQUA + message.get("group_id").getAsString()));
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Registered: " + ChatColor.AQUA + registered));
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + "Reputation: " + ChatColor.AQUA + message.get("reputation").getAsString()));

							// check if validated
							if(message.get("validated").getAsString().equals("1")){
			                	sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "Validated: " + ChatColor.GREEN + "Yes!"));
			                } else{
			                	sender.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GREEN + "Validated: " + ChatColor.RED + "No!"));
			                }
							// check if banned
							if( message.get("banned").getAsString().equals("1")){
			                	sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Banned: " + ChatColor.RED + "Yes!"));
			                } else{
			                	sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Banned: " + ChatColor.GREEN + "No!"));
			                }
							sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&3&m--------------------------------")));
						}

						// Close output/input stream
						outputStream.flush();
						outputStream.close();
						inputStream.close();

						// Disconnect
						connection.disconnect();

					} catch(Exception e) {
						// Exception
						e.printStackTrace();
					}
				}
			});

		} else {
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have permission to this command!"));
		}

		return;
	}	
}