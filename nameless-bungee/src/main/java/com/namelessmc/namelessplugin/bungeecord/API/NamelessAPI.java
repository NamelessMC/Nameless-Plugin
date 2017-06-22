package com.namelessmc.namelessplugin.bungeecord.API;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.Config.NamelessConfigManager;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessRegisterPlayer;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NamelessAPI {

	private NamelessPlugin plugin;
	private NamelessConfigManager namelessConfigManager;

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

	public NamelessRegisterPlayer registerPlayer(ProxiedPlayer player, String email) {
		NamelessRegisterPlayer register = new NamelessRegisterPlayer(plugin, player, email);
		return register;
	}

	public NamelessConfigManager getConfigManager() {
		namelessConfigManager = new NamelessConfigManager(plugin);
		return namelessConfigManager;
	}

	public CheckWebAPIConnection checkConnection() {
		CheckWebAPIConnection checkWebAPIConnection = new CheckWebAPIConnection(plugin);
		return checkWebAPIConnection;
	}
}
