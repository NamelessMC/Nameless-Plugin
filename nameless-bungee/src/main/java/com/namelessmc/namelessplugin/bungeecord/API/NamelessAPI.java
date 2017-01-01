package com.namelessmc.namelessplugin.bungeecord.API;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.Config.NamelessConfigs;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessRegisterPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NamelessAPI {

	NamelessPlugin plugin;
	NamelessConfigs namelessConfigs;

	public NamelessAPI(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public NamelessPlayer getPlayer(String id) {
		NamelessPlayer player = new NamelessPlayer(id, plugin);
		return player;
	}
	
	public NamelessRegisterPlayer registerPlayer(String userName, String uuid, String email){
		NamelessRegisterPlayer register = new NamelessRegisterPlayer(plugin, userName, uuid, email);
		return register;
	}

	public NamelessRegisterPlayer registerPlayer(ProxiedPlayer player, String email){
		NamelessRegisterPlayer register = new NamelessRegisterPlayer(plugin, player, email);
		return register;
	}

	public NamelessConfigs getConfigs() {
		namelessConfigs = new NamelessConfigs(plugin);
		return namelessConfigs;
	}
	
	public NamelessChat getChat(){
		NamelessChat chat = new NamelessChat(plugin);
		return chat;
	}
}
