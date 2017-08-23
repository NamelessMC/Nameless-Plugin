package com.namelessmc.namelessplugin.spigot.API;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.entity.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;

public class UpdateChecker {

	private NamelessPlugin plugin;
	private URL url;
	private HttpsURLConnection connection;
	private String title;
	// private String latestVersion;
	private String returnLatestV;
	private String thisVersion;
	// private Integer thisIntVersion;
	// private Integer thisIntLatestVersion;
	private String link;
	private boolean updateNeeded;

	public UpdateChecker(NamelessPlugin plugin) {
		this.plugin = plugin;

		try {
			url = new URL("https://plugin.namelessmc.com/versions.rss");
			connection = (HttpsURLConnection) url.openConnection();
			connection.addRequestProperty("User-Agent", "Mozilla/4.76");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean updateNeeded() {
		try {
			InputStream input = connection.getInputStream();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);

			Node latestFile = document.getElementsByTagName("item").item(0);
			NodeList children = latestFile.getChildNodes();

			// For normal versions

			/*
			 * returnLatestV =
			 * children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
			 * latestVersion =
			 * children.item(1).getTextContent().replaceAll("[^\\d]", "");
			 * thisVersion =
			 * plugin.getDescription().getVersion().replaceAll("[^\\d]", "");
			 * thisIntVersion = Integer.parseInt(thisVersion);
			 * thisIntLatestVersion = Integer.parseInt(latestVersion); link =
			 * children.item(3).getTextContent();
			 * 
			 * if (thisIntVersion < thisIntLatestVersion) { updateNeeded = true;
			 * } else if (thisIntVersion >= thisIntLatestVersion) { updateNeeded
			 * = false; } else { updateNeeded = false; }
			 */

			// For Pre Release (No integers) (Temporary)
			title = children.item(1).getTextContent();
			returnLatestV = children.item(3).getTextContent();
			thisVersion = plugin.getDescription().getVersion();
			link = children.item(5).getTextContent();

			if (!thisVersion.equalsIgnoreCase(returnLatestV)) {
				updateNeeded = true;
			} else {
				updateNeeded = false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return updateNeeded;
	}

	public String getTitle() {
		return this.title;
	}

	public String getVersion() {
		return this.returnLatestV;
	}

	public String getCurrentVersion() {
		return this.thisVersion;
	}

	public String getLink() {
		return this.link;
	}

	public void sendUpdateMessage(Player player) {
		if (plugin.isSpigot()) {
			player.sendMessage(NamelessChat.convertColors("&a&m------ &bNamelessMC&a&m ---------"));
			player.sendMessage(NamelessChat.convertColors("&6Found a new update"));
			player.sendMessage(NamelessChat.convertColors("&aNew version:&e " + getVersion()));
			player.sendMessage(NamelessChat.convertColors("&bYour version:&c " + getCurrentVersion()));
			player.spigot().sendMessage(NamelessChat.sendClickableMessage("&eGet it &chere", net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL,
					getLink(), net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, "&bClick to go to the download link"));
			player.sendMessage(NamelessChat.convertColors("&d" + getTitle()));
			player.sendMessage(NamelessChat.convertColors("&a&m--------------------------"));
		} else if (plugin.isBukkit()) {
			player.sendMessage(NamelessChat.convertColors("&a&m------ &bNamelessMC&a&m ---------"));
			player.sendMessage(NamelessChat.convertColors("&6Found a new update"));
			player.sendMessage(NamelessChat.convertColors("&aNew version:&e " + getVersion()));
			player.sendMessage(NamelessChat.convertColors("&bYour version:&c " + getCurrentVersion()));
			player.sendMessage(NamelessChat.convertColors("&2Get it at:&c " + getLink()));
			player.sendMessage(NamelessChat.convertColors("&d" + getTitle()));
			player.sendMessage(NamelessChat.convertColors("&a&m--------------------------"));
		}
	}
	
	public ArrayList<String> getConsoleUpdateMessage() {
		ArrayList<String> messages = new ArrayList<String>();
		messages.add("&6Found a new update");
		messages.add("&aNew version:&e " + getVersion());
		messages.add("&bYour version:&c " + getCurrentVersion());
		messages.add("&2Get it at:&c " + getLink());
		messages.add("&d" + getTitle());
		return messages;
	}
	
}
