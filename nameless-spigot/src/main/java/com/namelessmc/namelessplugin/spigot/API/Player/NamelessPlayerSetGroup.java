package com.namelessmc.namelessplugin.spigot.API.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.Utils.RequestUtil;

public class NamelessPlayerSetGroup extends RequestUtil {

	NamelessPlugin plugin;

	private String id;
	private Integer newGroup;

	public NamelessPlayerSetGroup(NamelessPlugin plugin, String id, Integer newGroup) {
		super(RequestType.Post, "setGroup", getPostString(id) + getPostStringGroup(newGroup.toString()));
		this.plugin = plugin;
		this.id = id;
		this.newGroup = newGroup;
	}

	public NamelessPlayerSetGroup(NamelessPlugin plugin, String id, String newGroup) {
		super(RequestType.Post, "setGroup", getPostString(id) + getPostStringGroup(newGroup));
		this.plugin = plugin;
		this.id = id;

		if (!hasError()) {
			this.newGroup = plugin.getAPI().getPlayer(id).getGroupID();
		} 
	}

	public String getID() {
		return id;
	}

	public Integer getNewGroup() {
		return newGroup;
	}

	private static String getPostStringGroup(String group) {
		String string = null;
		try {
			string = "&group_id=" + URLEncoder.encode(group.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return string;
	}

}