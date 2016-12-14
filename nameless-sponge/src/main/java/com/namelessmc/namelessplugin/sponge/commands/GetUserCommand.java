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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.namelessmc.namelessplugin.sponge.NamelessPlugin;

    /*
     * GetUser command made by IsS127
     */

public class GetUserCommand implements CommandExecutor {

	@Inject
	Game game;
	NamelessPlugin plugin;
	String permissionAdmin;

	/*
	 *  Handle inputted command
	 */
	@Override
	public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
		// check if player has permissionAdmin Permission
		if(sender.hasPermission(permissionAdmin + ".getuser")){
			// check if has set url.
			if(plugin.hasSetUrl == false){
				sender.sendMessage(Text.builder("Please set a API Url in the configuration!").color(TextColors.RED).build());
				return CommandResult.success();
			}

			// Try to get the user
			game.getScheduler().createAsyncExecutor(new Runnable(){
				@Override
				public void run(){
					// Ensure username or uuid set.
					if(args.toString().length() < 1 || args.toString().length() > 1){
						sender.sendMessage(Text.builder("Incorrect usage: /getuser username/uuid").color(TextColors.RED).build());
						return;
					}

					// Send POST request to API
					try {

						// Create string containing POST contents
						String toPostStringUName = 	"username=" + URLEncoder.encode(args.getOne("player").get().toString(), "UTF-8");
						String toPostStringUUID = 	"uuid=" + URLEncoder.encode(args.getOne("player").get().toString(), "UTF-8");

						URL apiConnection = new URL(plugin.getAPIUrl() + "/get");

						HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
						connection.setRequestMethod("POST");

						// check if player typed uuid or username
						if(args.getOne("player").get().toString().length() >= 17){
							connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUUID.length()));
						} else if(args.getOne("player").get().toString().length() <= 16){
							connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUName.length()));
						}

						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						connection.setDoOutput(true);
						connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

						DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

						// Write request
						// check if player typed uuid or username
						if(args.getOne("player").get().toString().length() >= 17){
							outputStream.writeBytes(toPostStringUUID);
						} else if(args.getOne("player").get().toString().length() <= 16){
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
							sender.sendMessage(Text.builder("Error: " + response.get("message").toString()).color(TextColors.RED).build());
						} else {

							// Convert UNIX timestamp to date
							java.util.Date registered = new java.util.Date(Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

							// Display get user.
							sender.sendMessage(Text.builder("--------------------------------").color(TextColors.DARK_AQUA).style(TextStyles.STRIKETHROUGH).build());
							sender.sendMessage(TextTemplate.of(Text.builder("Username: ").color(TextColors.GREEN).build(), Text.builder(message.get("username").getAsString()).color(TextColors.AQUA).build()));
							sender.sendMessage(TextTemplate.of(Text.builder("DisplayName: ").color(TextColors.GREEN).build(), Text.builder(message.get("displayname").getAsString()).color(TextColors.AQUA).build()));
							sender.sendMessage(TextTemplate.of(Text.builder("UUID: ").color(TextColors.GREEN).build(), Text.builder(message.get("uuid").getAsString()).color(TextColors.AQUA).build()));
							sender.sendMessage(TextTemplate.of(Text.builder("Group ID: ").color(TextColors.GREEN).build(), Text.builder(message.get("group_id").getAsString()).color(TextColors.AQUA).build()));
							sender.sendMessage(TextTemplate.of(Text.builder("Registered: ").color(TextColors.GREEN).build(), Text.builder(registered.toString()).color(TextColors.AQUA).build()));
							sender.sendMessage(TextTemplate.of(Text.builder("Reputation: ").color(TextColors.GREEN).build(), Text.builder(message.get("reputation").getAsString()).color(TextColors.AQUA).build()));

							// check if validated
							if(message.get("validated").getAsString().equals("1")){
			                	sender.sendMessage(TextTemplate.of(Text.builder("Validated: ").color(TextColors.DARK_GREEN).build(), Text.builder("Yes!").color(TextColors.GREEN).build()));
			                } else{
			                	sender.sendMessage(TextTemplate.of(Text.builder("Validated: ").color(TextColors.DARK_GREEN).build(), Text.builder("No!").color(TextColors.RED).build()));
			                }
							// check if banned
							if( message.get("banned").getAsString().equals("1")){
			                	sender.sendMessage(TextTemplate.of(Text.builder("Banned: ").color(TextColors.RED).build(), Text.builder("Yes!").color(TextColors.RED).build()));
			                } else{
			                	sender.sendMessage(TextTemplate.of(Text.builder("Banned: ").color(TextColors.RED).build(), Text.builder("No!").color(TextColors.GREEN).build()));
			                }
							sender.sendMessage(Text.builder("--------------------------------").color(TextColors.DARK_AQUA).style(TextStyles.STRIKETHROUGH).build());
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
			sender.sendMessage(Text.builder("You don't have permission to this command!").color(TextColors.RED).build());
		}

		return CommandResult.success();
	}

}