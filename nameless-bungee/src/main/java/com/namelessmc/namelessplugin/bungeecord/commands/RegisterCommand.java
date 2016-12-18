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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 * Register CMD for BungeeCord by IsS127
 */
    
public class RegisterCommand extends Command {

	NamelessPlugin plugin;
	String permission;

	public RegisterCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission =  pluginInstance.permission;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permission Permission & ensure who inputted command is a Player
		if(sender instanceof ProxiedPlayer && sender.hasPermission(permission + ".register")){
			// check if hasSetUrl
			if(!plugin.hasSetUrl){
				sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Please set a API Url in the configuration!"));
				return;
			}

			ProxiedPlayer player = (ProxiedPlayer) sender;

			// Try to register user
			ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(args.length < 1 || args.length > 1){
						player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Incorrect usage: /register email"));
						return;
					}

					// Send POST request to API
					try {

						// Create string containing POST contents
						String toPostString = 	"username=" + URLEncoder.encode(player.getName(), "UTF-8") +
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
							player.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "Error: " + response.get("message").getAsString()));
						} else {
							// Display success message to user
							player.sendMessage(TextComponent.fromLegacyText(ChatColor.GREEN + response.get("message").getAsString()));
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

		} else if(!sender.hasPermission(permission + ".register")){
			sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RED + "You don't have permission to this command!"));
		} else {
			sender.sendMessage(TextComponent.fromLegacyText("You must be ingame to use this command."));			
		}

		return;
	}
}