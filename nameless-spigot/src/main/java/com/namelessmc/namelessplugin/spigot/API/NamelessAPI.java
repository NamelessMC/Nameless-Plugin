package com.namelessmc.namelessplugin.spigot.API;

import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.Config.NamelessConfigManager;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessRegisterPlayer;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.ReflectionUtil;

public class NamelessAPI {

	private NamelessPlugin plugin;
	private NamelessConfigManager namelessConfigManager;
	private ReflectionUtil reflection;

	public NamelessAPI(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public NamelessPlayer getPlayer(String id) {
		NamelessPlayer player = new NamelessPlayer(id, plugin);
		return player;
	}

	public NamelessRegisterPlayer registerPlayer(String userName, String uuid, String email) {
		NamelessRegisterPlayer register = new NamelessRegisterPlayer(plugin, userName, uuid, email);
		return register;
	}

	public NamelessRegisterPlayer registerPlayer(Player player, String email) {
		NamelessRegisterPlayer register = new NamelessRegisterPlayer(plugin, player, email);
		return register;
	}

	public NamelessConfigManager getConfigManager() {
		namelessConfigManager = new NamelessConfigManager(plugin);
		return namelessConfigManager;
	}

	public NamelessChat getChat() {
		NamelessChat chat = new NamelessChat(plugin);
		return chat;
	}

	public ReflectionUtil getReflection() {
		return reflection;
	}

	public CheckWebAPIConnection checkConnection() {
		CheckWebAPIConnection checkWebAPIConnection = new CheckWebAPIConnection(plugin);
		return checkWebAPIConnection;
	}
}
