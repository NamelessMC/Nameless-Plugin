package com.namelessmc.api.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PostString {
	
	public static String urlEncodeString(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/*public static String getPlayerPostString(UUID uuid) {
		try {
			return "uuid=" + URLEncoder.encode(uuid.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}*/
	
	/*public static String getGroupPostString(String groupName) {
		String string = null;
		try {
			string = "&group_id=" + URLEncoder.encode(groupName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return string;
	}*/

}
