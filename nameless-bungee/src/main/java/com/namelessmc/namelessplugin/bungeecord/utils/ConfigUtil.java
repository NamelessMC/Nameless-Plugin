package com.namelessmc.namelessplugin.bungeecord.utils;

import net.md_5.bungee.config.Configuration;

public class ConfigUtil {
	
    public boolean contains(Configuration file, String contain){
        return file.get(contain) != null;
    }
	
}
