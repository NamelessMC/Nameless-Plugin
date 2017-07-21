package com.namelessmc.namelessplugin.spigot.API.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.namelessmc.namelessplugin.spigot.API.Utils.RequestUtil;

public class NamelessPlayerUpdateUsername extends RequestUtil{

	public NamelessPlayerUpdateUsername(String id, String newUsername) {
		super(RequestType.Post, "updateUsername", getPostStringUsername(id, newUsername));
	}
	
	private static String getPostStringUsername(String id, String newUsername){

	}

}