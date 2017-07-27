/* Copyright © 2016 Acquized <Acquized@users.noreply.github.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See the LICENSE.txt file for more details.
 */
package com.namelessmc.plugin.NamelessSpigot.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

/**
 * Simple JSON message creation in minecraft
 * @author Acquized
 * @version 1.0
 */
public class Json {

    private ArrayList<JsonMessage> messages = new ArrayList<>();

    /**
     * Appends a JsonMessage to the current Json array
     * @param msg JsonMessage
     * @return this class
     */
    public Json append(JsonMessage msg) {
        messages.add(msg);
        return this;
    }

    /**
     * Sets a JsonMessage at the specified index to the Json array
     * @param msg JsonMessage
     * @param index index in the Json array
     * @return this class
     */
    public Json set(JsonMessage msg, int index) {
        messages.add(index, msg);
        return this;
    }

    /**
     * Sends the Json message to specified players
     * @param players Array or object of a player
     * @return this class
     */
    public Json send(Player... players) {
        if(messages.size() > 0) {
            try {
                for(Player p : players) {
                    Object serializer = Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + ".ChatSerializer").getMethod("a", String.class).invoke(null, toString());
                    Object packet = Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + "PacketPlayOutChat").getConstructor(Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + "IChatBaseComponent")).newInstance(serializer);
                    Object handle = p.getClass().getMethod("getHandle").invoke(p);
                    Object connection = handle.getClass().getField("playerConnection").get(handle);
                    connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + "Packet")).invoke(connection, packet);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException | ClassNotFoundException | InstantiationException ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }

    /**
     * Sends the Json message to all players
     * @return this class
     */
    public Json send() {
        send(Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]));
        return this;
    }

    /**
     * Converts the current Json array to a string
     * @return JSON string
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[\"\"");
        if(messages.size() > 0) {
            for(JsonMessage jm : messages) {
                builder.append(jm.toString());
            }
        }
        return builder.toString() + "]";
    }

    /**
     * Escapes the string to fit into Json values. Used internally.
     * @param text text to escape
     * @return escaped String
     */
    public static String escape(String text) {
        return JSONValue.escape(text);
    }

    /**
     * Creates a new Json creator
     * @return new Json array creator
     */
    public static Json create() {
        return new Json();
    }

    public static class JsonMessage {

        private String text = "Argument 'text' not found.";

        private ChatColor color = ChatColor.WHITE;
        private ChatFormatting formatting = ChatFormatting.RESET;

        private Object[] hoverAction = {};
        private Object[] clickAction = {};

        /**
         * Sets the text in the Json message
         * @param text Text
         * @return this class
         */
        public JsonMessage text(String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the text color in the Json message
         * @param color Color
         * @return this class
         */
        public JsonMessage color(ChatColor color) {
            this.color = color;
            return this;
        }

        /**
         * Sets the formatting of the Json message
         * @param formatting Formatting
         * @return this class
         */
        public JsonMessage formatting(ChatFormatting formatting) {
            this.formatting = formatting;
            return this;
        }

        /**
         * Sets the hover action of the Json message
         * @param action hover action
         * @param value hover value
         * @return this class
         */
        public JsonMessage onHover(HoverAction action, String value) {
            hoverAction[0] = action;
            hoverAction[1] = value;
            return this;
        }

        /**
         * Sets the click action of the Json message
         * @param action click action
         * @param value click value
         * @return this class
         */
        public JsonMessage onClick(ClickAction action, String value) {
            clickAction[0] = action;
            clickAction[1] = value;
            return this;
        }

        /**
         * Converts the message in a string which can be appended in the json array
         * @return Json string
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(",{\"text\":\"" + Json.escape(text) + "\",\"color\":\"" + color.name().toLowerCase() + "\",");
            if(formatting != ChatFormatting.RESET) {
                builder.append("\"").append(formatting.color.name().toLowerCase()).append("\":true,");
            }
            if(clickAction.length > 0) {
                builder.append("\"clickEvent\":{\"action\":\"").append(((ClickAction) clickAction[0]).name().toLowerCase()).append("\",\"value\":\"").append(Json.escape((String) clickAction[1])).append("\"},");
            }
            if(hoverAction.length > 0) {
                if(hoverAction[0] != HoverAction.SHOW_TEXT) {
                    builder.append("\"hoverEvent\":{\"action\":\"").append(((HoverAction) hoverAction[0]).name().toLowerCase()).append("\",\"value\":\"").append(hoverAction[1]).append("\"},");
                } else {
                    builder.append("\"hoverEvent\":{\"action\":\"").append(((HoverAction) hoverAction[0]).name().toLowerCase()).append("\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"").append(Json.escape((String) hoverAction[1])).append("\"}]}},"); // TODO: Maybe make also customizeable
                }
            }
            String json = builder.toString();
            if(json.endsWith(",")) {
                json = json.substring(0, json.length() - 1);
            }
            return json + "}";
        }

        /**
         * Creates new Json message
         * @return new Json message
         */
        public static JsonMessage newMessage() {
            return new JsonMessage();
        }

    }

    public static class ChatFormatting {

        public static final ChatFormatting ITALIC = new ChatFormatting(ChatColor.ITALIC);
        public static final ChatFormatting OBFUSCATED = new ChatFormatting(ChatColor.MAGIC);
        public static final ChatFormatting BOLD = new ChatFormatting(ChatColor.BOLD);
        public static final ChatFormatting STRIKETHROUGH = new ChatFormatting(ChatColor.STRIKETHROUGH);
        public static final ChatFormatting UNDERLINE = new ChatFormatting(ChatColor.UNDERLINE);
        public static final ChatFormatting RESET = new ChatFormatting(ChatColor.RESET);

        private ChatColor color;

        public ChatFormatting(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }

    }

    public enum HoverAction {
        SHOW_TEXT, SHOW_ACHIEVEMENT, SHOW_ITEM, SHOW_ENTITY
    }

    public enum ClickAction {
        OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE
    }

}