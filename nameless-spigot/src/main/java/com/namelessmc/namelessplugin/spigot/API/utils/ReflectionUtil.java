package com.namelessmc.namelessplugin.spigot.API.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class ReflectionUtil {

	public Class<?> getNMSClass(String nmsClassName) throws Exception {
		String version = Bukkit.getServer().getClass().getPackage().getName().split(".")[3];
		String name = "net.minecraft.server." + version + "." + nmsClassName;

		Class<?> nmsClass = Class.forName(name);
		return nmsClass;
	}

	public Object getConnection(Player p) throws Exception {
		Method getHandle = p.getClass().getMethod("getHandle");
		Object nmsPlayer = getHandle.invoke(p);
		Field conField = nmsPlayer.getClass().getField("playerConnection");
		Object con = conField.get(nmsPlayer);
		return con;
	}

	public Object sendMessage(Player p, BaseComponent component) throws Exception {
		Class<?> packetClass = getNMSClass("PacketPlayOutChat");
		Class<?> icbc = getNMSClass("IChatBaseComponent");
		Class<?> serializer = getNMSClass("IChatBaseComponent$ChatSerializer");

		Constructor<?> packetConstructor = packetClass.getConstructor(icbc);

		Object text = serializer.getMethod("a", String.class).invoke(serializer,
				ComponentSerializer.toString(component));
		Object packet = packetConstructor.newInstance(text);

		Field a = packetClass.getDeclaredField("a");
		a.setAccessible(true);
		a.set(packet, text);

		Object con = getConnection(p);
		return con.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(con, packet);
	}

}