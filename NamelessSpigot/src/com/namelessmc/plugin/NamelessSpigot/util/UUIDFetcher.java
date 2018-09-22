package com.namelessmc.plugin.NamelessSpigot.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Helper-class for getting UUIDs of players
 */
public class UUIDFetcher {
	
	/**
	 * @param player The player
	 * @return The UUID of the given player
	 */
	public static UUID getUUID(Player player) {
		return getUUID(player.getName());
	}
	
	/**
	 * @param playerName The name of the player
	 * @return The UUID of the given player
	 */
	public static UUID getUUID(String playerName) {
		// First check if the player is online
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName().equals(playerName)) {
				return player.getUniqueId();
			}
		}
		
		String output = callURL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
		
		StringBuilder result = new StringBuilder();
		
		readData(output, result);
		
		String u = result.toString();
		
		String uuid = "";
		
		for(int i = 0; i <= 31; i++) {
			if(i >= u.length())
				break;

			uuid = uuid + u.charAt(i);
			if(i == 7 || i == 11 || i == 15 || i == 19) {
				uuid = uuid + "-";
			}
		}
		
		return UUID.fromString(uuid);
	}
	
	private static void readData(String toRead, StringBuilder result) {
		int i = 7;
		
		while(i < 200) {
			if(i >= toRead.length())
				break;

			if(!String.valueOf(toRead.charAt(i)).equalsIgnoreCase("\"")) {
				
				result.append(String.valueOf(toRead.charAt(i)));
				
			} else {
				break;
			}
			
			i++;
		}
	}
	
	private static String callURL(String URL) {
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(URL);
			urlConn = url.openConnection();
			
			if (urlConn != null) urlConn.setReadTimeout(60 * 1000);
			
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				
				if (bufferedReader != null) {
					int cp;
					
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					
					bufferedReader.close();
				}
			}
			
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
 
		return sb.toString();
	}
}