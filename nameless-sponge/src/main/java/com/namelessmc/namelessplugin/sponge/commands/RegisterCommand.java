package com.namelessmc.namelessplugin.sponge.commands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.namelessmc.namelessplugin.sponge.NamelessPlugin;

     /*
      * Bungeecord version made by IsS127
      */
    
public class RegisterCommand implements CommandExecutor {

	@Inject
	Game game;
	NamelessPlugin plugin;
	String permission;

	@Override
	public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
		// check if player has permission Permission
		if(sender.hasPermission(permission + ".register")){
			// check if hasSetUrl
			if(plugin.hasSetUrl == false){
				sender.sendMessage(Text.of(TextColors.RED, "Please set a API Url in the configuration!"));
				return CommandResult.success();
			}

			// Ensure user who inputted command is player and not console
			if(sender instanceof Player){
				Player player = (Player) sender;

				// Try to register user
				game.getScheduler().createAsyncExecutor(new Runnable(){
					@Override
					public void run(){
						// Ensure email is set
						if(args.toString().length() < 1 || args.toString().length() > 1){
							player.sendMessage(Text.of(TextColors.RED, "Incorrect usage: /register email"));
							return;
						}

						// Send POST request to API
						try {

							// Create string containing POST contents
							String toPostString = 	"username=" + URLEncoder.encode(player.getName(), "UTF-8") +
													"&email=" + URLEncoder.encode(args.getOne("e-mail").get().toString(), "UTF-8") + 
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
								player.sendMessage(Text.of(TextColors.RED, "Error: " + response.get("message")));
							} else {
								// Display success message to user
								player.sendMessage(Text.of(TextColors.GREEN, response.get("message")));
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
				sender.sendMessage(Text.of("You must be ingame to use this command."));
			}
				
		} else {
			sender.sendMessage(Text.of(TextColors.RED, "You don't have permission to this command!"));
		}

		return CommandResult.success();
	}

}