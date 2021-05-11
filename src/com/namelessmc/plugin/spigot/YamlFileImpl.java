package com.namelessmc.plugin.spigot;

import java.nio.file.Path;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.plugin.common.AbstractYamlFile;

public class YamlFileImpl extends AbstractYamlFile {

	private final YamlConfiguration config;

	public YamlFileImpl(final Path file) {
		this.config = YamlConfiguration.loadConfiguration(file.toFile());
	}

	@Override
	public String getString(final String path) {
		return this.config.getString(path);
	}

}
