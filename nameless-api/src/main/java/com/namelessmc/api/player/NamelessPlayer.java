package com.namelessmc.api.player;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.api.utils.RequestUtil;
import com.namelessmc.api.utils.RequestUtil.Request;
import com.namelessmc.api.utils.RequestUtil.RequestType;

public class NamelessPlayer {

	private String userName;
	private String displayName;
	private String uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean validated;
	private boolean banned;

	public NamelessPlayer(UUID uuid, URL baseUrl, boolean https) {
		Request request = RequestUtil.createRequest(RequestType.POST, baseUrl, RequestUtil.getPostString(uuid), https);
		
		JsonParser parser = new JsonParser();
		JsonObject response = request.getResponse();
		
		if (!request.hasSucceeded()) {
			exists = false;
			return;
		}

		//No errors, parse response
		
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();

		exists = true;

		// Convert UNIX timestamp to date
		Date registered = new Date(Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

		// Display get user.
		userName = message.get("username").getAsString();
		displayName = message.get("displayname").getAsString();
		uuid = UUID.fromString(message.get("uuid").getAsString());
		groupID = message.get("group_id").getAsInt();
		registeredDate = registered;
		reputation = message.get("reputation").getAsInt();
		validated = message.get("validated").getAsString().equals("1");
		banned = message.get("banned").getAsString().equals("1");
	}

	public String getUsername() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return userName;
	}

	public String getDisplayName() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return displayName;
	}

	public String getUUID() {
		return uuid;
	}

	public int getGroupID() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return groupID;
	}

	public int getReputations() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return reputation;
	}

	public Date getRegisteredDate() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return registeredDate;
	}

	public boolean exists() {	
		return exists;
	}

	public boolean isValidated() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return validated;
	}

	public boolean isBanned() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return banned;
	}
	
	public NamelessPlayerNotifications getNotifications() {
		NamelessPlayerNotifications notificaitons = new NamelessPlayerNotifications(id); // TODO Fix this
		return notificaitons;
	}

	public NamelessPlayerSetGroup setGroupID(int newGroup) {
		NamelessPlayerSetGroup setGroup = new NamelessPlayerSetGroup(plugin, id, newGroup);// TODO Fix this
		return setGroup;
	}

	public NamelessPlayerSetGroup setGroupID(String newGroup) {
		NamelessPlayerSetGroup setGroup = new NamelessPlayerSetGroup(plugin, id, newGroup);// TODO Fix this
		return setGroup;
	}

	public NamelessPlayerUpdateUsername updateUsername(String newUserName) {
		NamelessPlayerUpdateUsername updateUsername = new NamelessPlayerUpdateUsername(id, newUserName);// TODO Fix this
		return updateUsername;
	}

	public NamelessReportPlayer reportPlayer(String[] args) {
		NamelessReportPlayer report = new NamelessReportPlayer(uuid, args);// TODO Fix this
		return report;
	}

}