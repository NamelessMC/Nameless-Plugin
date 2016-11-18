package com.namelessmc.namelessplugin;

import net.md_5.bungee.api.plugin.Plugin;
import static net.md_5.bungee.api.ChatColor.RED;

public class Bungee extends Plugin {

    @Override
    public void onEnable() {
        getLogger().severe(RED + "===============================================");
        getLogger().severe(RED + "NamelessMC is NOT a Bungeecord plugin yet");
        getLogger().severe(RED + "Install this plugin on your spigot/bukkit server");
        getLogger().severe(RED + "===============================================");
    }
}
