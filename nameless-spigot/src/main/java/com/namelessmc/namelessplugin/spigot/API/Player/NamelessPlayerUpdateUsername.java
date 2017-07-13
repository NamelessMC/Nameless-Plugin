package com.namelessmc.namelessplugin.spigot.API.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.namelessmc.namelessplugin.spigot.API.utils.RequestUtil;

public class NamelessPlayerUpdateUsername extends RequestUtil{

	public NamelessPlayerUpdateUsername(String id, String newUsername) {
		super(RequestType.Post, "updateUsername", getPostStringUsername(id, newUsername));
	}
	
	private static String getPostStringUsername(String id, String newUsername){
		String string = null;
		try {
			string = "id=" + URLEncoder.encode(id, "UTF-8") + "&new_username="
					+ URLEncoder.encode(newUsername, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return string;
	}
}
