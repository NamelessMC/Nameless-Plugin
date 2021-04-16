package com.namelessmc.plugin.spigot;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.plugin.common.AbstractYamlFile;

public class YamlFileImpl extends AbstractYamlFile {

	private final YamlConfiguration config;

	public YamlFileImpl(final YamlConfiguration config) {
		this.config = config;
	}

	@Override
	public String getString(final String path) {
		return this.config.getString(path);
	}

}
