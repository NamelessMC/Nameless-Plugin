package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.common.AbstractYamlFile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class YamlFileImpl extends AbstractYamlFile {

	private final @NotNull YamlConfiguration config;

	@Deprecated
	public YamlFileImpl(final @NotNull Path file) {
		this.config = YamlConfiguration.loadConfiguration(file.toFile());
	}

	public YamlFileImpl(final @NotNull YamlConfiguration config) {
		this.config = config;
	}

	@Override
	public String getString(final String path) {
		return this.config.getString(path);
	}

	@Override
	public boolean isString(String path) {
		return this.config.isString(path);
	}

}
