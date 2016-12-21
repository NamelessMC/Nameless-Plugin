package com.namelessmc.namelessplugin.spigot.nms;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;

public class v1_8_R3 implements NMSManager {

	@Override
	public void sendMessage(Player p, BaseComponent component) {
		CraftPlayer cp =(CraftPlayer) p;
		IChatBaseComponent chat = IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(component));
		cp.getHandle().sendMessage(chat);
	}

}
