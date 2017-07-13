package com.namelessmc.namelessplugin.spigot.API.Player;

import java.util.Date;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.Utils.RequestUtil;

public class NamelessPlayer extends RequestUtil {

	private NamelessPlugin plugin;
	private String id;

	private String userName;
	private String displayName;
	private String uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean validated;
	private boolean banned;

	public NamelessPlayer(String id, NamelessPlugin plugin) {
		super(RequestType.Post, "get", getPostString(id));
		// Reigster Plugin
		this.plugin = plugin;
		this.id = id;
		JsonParser parser = new JsonParser();
		JsonObject response = getResponse();
		JsonObject message = new JsonObject();

		// check if there isnt any error, if so parse the messages.
		if (!hasError()) {
			message = parser.parse(response.get("message").getAsString()).getAsJsonObject();
		}

		if (hasError()) {
			// Error with request
			exists = false;
		} else {

			exists = true;

			// Convert UNIX timestamp to date
			Date registered = new Date(
					Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

			// Display get user.
			userName = message.get("username").getAsString();
			displayName = message.get("displayname").getAsString();
			uuid = message.get("uuid").getAsString();
			groupID = message.get("group_id").getAsInt();
			registeredDate = registered;
			reputation = message.get("reputation").getAsInt();

			// check if validated
			if (message.get("validated").getAsString().equals("1")) {
				validated = true;
			} else {
				validated = false;
			}
			// check if banned
			if (message.get("banned").getAsString().equals("1")) {
				banned = true;
			} else {
				banned = false;
			}

		}

	}

	public String getUserName() {
		return userName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUUID() {
		return uuid;
	}

	public Integer getGroupID() {
		return groupID;
	}

	public Integer getReputations() {
		return reputation;
	}

	public Date getRegisteredDate() {
		return registeredDate;
	}

	// Checks if player exists or not.
	public boolean exists() {
		return exists;
	}

	public boolean isValidated() {
		return validated;
	}

	public boolean isBanned() {
		return banned;
	}
	
	public NamelessPlayerNotifications getNotifications() {
		NamelessPlayerNotifications notificaitons = new NamelessPlayerNotifications(id);
		return notificaitons;
	}

	public NamelessPlayerSetGroup setGroupID(int newGroup) {
		NamelessPlayerSetGroup setGroup = new NamelessPlayerSetGroup(plugin, id, newGroup);
		return setGroup;
	}

	public NamelessPlayerSetGroup setGroupID(String newGroup) {
		NamelessPlayerSetGroup setGroup = new NamelessPlayerSetGroup(plugin, id, newGroup);
		return setGroup;
	}

	public NamelessPlayerUpdateUsername updateUsername(String newUserName) {
		NamelessPlayerUpdateUsername updateUsername = new NamelessPlayerUpdateUsername(id, newUserName);
		return updateUsername;
	}

	public NamelessReportPlayer reportPlayer(String[] args) {
		NamelessReportPlayer report = new NamelessReportPlayer(uuid, args);
		return report;
	}

}