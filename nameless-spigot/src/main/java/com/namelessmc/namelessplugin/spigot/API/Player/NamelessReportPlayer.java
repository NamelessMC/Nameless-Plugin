package com.namelessmc.namelessplugin.spigot.API.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.API.utils.MojangPlayerRequest;
import com.namelessmc.namelessplugin.spigot.API.utils.RequestUtil;

public class NamelessReportPlayer extends RequestUtil {

	public NamelessReportPlayer(String reporterUUID, String[] args) {
		super(RequestType.Post, "createReport", getReportedPostString(args[0] + getPostStringReporter(reporterUUID, args)));
	}

	private static String getReportedPostString(String playerName) {
		Player reported = Bukkit.getPlayerExact(playerName);
		String string = null;
		try {
			if (reported == null) {
				MojangPlayerRequest mPlayer = new MojangPlayerRequest(playerName);
				string = "reported_username=" + URLEncoder.encode(playerName, "UTF-8") + "&reported_uuid="
						+ URLEncoder.encode(mPlayer.getUUID(), "UTF-8");

			} else {
				string = "reported_username=" + URLEncoder.encode(playerName, "UTF-8") + "&reported_uuid="
						+ URLEncoder.encode(reported.getUniqueId().toString(), "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return string;
	}
	
	private static String getPostStringReporter(String reporterUUID, String[] args){
		String content = "";
		for (int i = 1; i < args.length; i++) {
			content += " " + args[i];
		}
		
		String string = null;
		try {
			string = "&reporter_uuid=" + URLEncoder.encode(reporterUUID, "UTF-8") + "&content="
					+ URLEncoder.encode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return string;
	}

}
