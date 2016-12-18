package com.namelessmc.namelessplugin.sponge.commands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.sponge.NamelessPlugin;

/*
 *  GetUser CMD by IsS127 (Sponge'd by Lmmb74)
 */

public class GetUserCommand implements CommandExecutor {

	NamelessPlugin plugin = NamelessPlugin.getInstance();

	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		// check if player has permissionAdmin Permission
		if(src instanceof ConsoleSource || (src instanceof Player && src.hasPermission(plugin.permissionAdmin + ".getuser"))){

			Task.builder().execute(new Runnable(){
				@Override
				public void run(){
					// Ensure username or uuid set.
					if(ctx.toString().length() < 1 || ctx.toString().length() > 1){
						src.sendMessage(Text.of(TextColors.RED, "Incorrect usage: /getuser username/uuid"));
						return;
					}

					// Send POST request to API
					try {

						// Create string containing POST contents
						String toPostStringUName = 	"username=" + URLEncoder.encode(ctx.<String>getOne("player").get(), "UTF-8");
						String toPostStringUUID = 	"uuid=" + URLEncoder.encode(ctx.<String>getOne("player").get(), "UTF-8");

						URL apiConnection = new URL(plugin.getAPIUrl() + "/get");

						HttpURLConnection connection = (HttpURLConnection) apiConnection.openConnection();
						connection.setRequestMethod("POST");

						// check if player typed uuid or username
						if(ctx.<String>getOne("player").get().length() >= 17){
							connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUUID.length()));
						} else if(ctx.<String>getOne("player").get().length() <= 16){
							connection.setRequestProperty("Content-Length", Integer.toString(toPostStringUName.length()));
						}

						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						connection.setDoOutput(true);
						connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

						DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

						// Write request
						// check if player typed uuid or username
						if(ctx.<String>getOne("player").get().length() >= 17){
							outputStream.writeBytes(toPostStringUUID);
						} else if(ctx.<String>getOne("player").get().length() <= 16){
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
							src.sendMessage(Text.builder("Error: " + response.get("message").toString()).color(TextColors.RED).build());
						} else {

							// Convert UNIX timestamp to date
							java.util.Date registered = new java.util.Date(Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

							// Display get user.
							src.sendMessage(Text.of(TextStyles.STRIKETHROUGH, TextColors.DARK_AQUA, "--------------------------------"));
							src.sendMessage(Text.of(TextColors.GREEN, "Username: ", TextColors.AQUA, message.get("username").getAsString()));
							src.sendMessage(Text.of(TextColors.GREEN, "DisplayName: ",TextColors.AQUA, message.get("displayname").getAsString()));
							src.sendMessage(Text.of(TextColors.GREEN, "UUID: ",TextColors.AQUA, message.get("uuid").getAsString()));
							src.sendMessage(Text.of(TextColors.GREEN, "Group ID: ",TextColors.AQUA, message.get("group_id").getAsString()));
							src.sendMessage(Text.of(TextColors.GREEN, "Registered: ",TextColors.AQUA, registered.toString()));
							src.sendMessage(Text.of(TextColors.GREEN, "Reputation: ",TextColors.AQUA, message.get("reputation").getAsString()));

							// check if validated
							if(message.get("validated").getAsString().equals("1")){
			                	src.sendMessage(Text.of(Text.builder("Validated: ").color(TextColors.DARK_GREEN).build(), Text.builder("Yes!").color(TextColors.GREEN).build()));
			                } else{
			                	src.sendMessage(Text.of(Text.builder("Validated: ").color(TextColors.DARK_GREEN).build(), Text.builder("No!").color(TextColors.RED).build()));
			                }
							// check if banned
							if( message.get("banned").getAsString().equals("1")){
			                	src.sendMessage(Text.of(TextColors.RED, "Banned: ", TextColors.RED, "Yes!"));
			                } else{
			                	src.sendMessage(Text.of(TextColors.RED, "Banned: ", TextColors.GREEN, "No!"));
			                }
							src.sendMessage(Text.of(TextStyles.STRIKETHROUGH, TextColors.DARK_AQUA, "--------------------------------"));
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
			}).submit(plugin);

		} else {
			src.sendMessage(Text.of(TextColors.RED, "You don't have permission to this command!"));
		}

		return CommandResult.success();
	}

}