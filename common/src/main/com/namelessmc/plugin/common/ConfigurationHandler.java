package com.namelessmc.plugin.common;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import xyz.derkades.derkutils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationHandler implements Reloadable {

	private static final @NonNull String[] ALL_CONFIG_NAMES = {
			"commands",
			"main",
			"modules",
	};

	private final @NonNull Path dataDirectory;
	private final @NonNull Map<String, CommentedConfigurationNode> configs = new HashMap<>();

	public ConfigurationHandler(final @NonNull Path dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public @NonNull CommentedConfigurationNode commands() {
		return this.getConfig("commands");
	}

	public @NonNull CommentedConfigurationNode main() {
		return this.getConfig("main");
	}

	public @NonNull CommentedConfigurationNode modules() {
		return this.getConfig("modules");
	}

	private @NonNull CommentedConfigurationNode getConfig(final String name) {
		final CommentedConfigurationNode config = this.configs.get(name);
		if (config == null) {
			throw new IllegalStateException(name + " config requested before it was loaded");
		}
		return config;
	}

	@Override
	public void unload() {

	}

	@Override
	public void load() {
		if (Files.exists(this.dataDirectory.resolve("config.yml"))) {
			throw new RuntimeException("Plugin directory contains config.yml. This is an old config file from the 2.x.x plugin versions. Please delete the plugin configuration directory or move it elsewhere to get new configuration files.");
		}

		try {
			Files.createDirectories(dataDirectory);
			for (final String configName : ALL_CONFIG_NAMES) {
				this.configs.put(configName, copyFromJarAndLoad(configName + ".yaml"));
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CommentedConfigurationNode copyFromJarAndLoad(final @NonNull String name) throws IOException {
		Path path = dataDirectory.resolve(name);
		FileUtils.copyOutOfJar(ConfigurationHandler.class, name, path);
		return YamlConfigurationLoader.builder().path(path).build().load();
	}

	public static @Nullable Duration getDuration(final ConfigurationNode node) {
		String string = node.getString();
		if (string == null) {
			return null;
		}
		try {
			return Duration.parse(string);
		} catch (final DateTimeParseException e) {
			return null;
		}
	}

}
