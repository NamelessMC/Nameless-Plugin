package com.namelessmc.plugin.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import com.namelessmc.plugin.common.logger.AbstractLogger;

public class ConfigurationHandler implements Reloadable {

	private static final String[] ALL_CONFIG_NAMES = {
			"commands",
			"main",
			"modules",
	};

	private final Path dataDirectory;
	private final Map<String, CommentedConfigurationNode> configs = new HashMap<>();

	public ConfigurationHandler(final Path dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public CommentedConfigurationNode commands() {
		return this.getConfig("commands");
	}

	public CommentedConfigurationNode main() {
		return this.getConfig("main");
	}

	public CommentedConfigurationNode modules() {
		return this.getConfig("modules");
	}

	private CommentedConfigurationNode getConfig(final String name) {
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
			Files.createDirectories(this.dataDirectory);
			for (final String configName : ConfigurationHandler.ALL_CONFIG_NAMES) {
				this.configs.put(configName, this.copyFromJarAndLoad(configName + ".yaml"));
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void postLoadChecks(final AbstractLogger logger) {

	}

	private CommentedConfigurationNode copyFromJarAndLoad(final String name) throws IOException {
		final Path path = this.dataDirectory.resolve(name);
		if (!Files.exists(path)) {
			try (InputStream in = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(name),
					"path does not exist in jar: " + name)) {
				Files.copy(in, path);
			}
		}
		return YamlConfigurationLoader.builder().path(path).build().load();
	}

	public static @Nullable Duration getDuration(final ConfigurationNode node) {
		final String string = node.getString();
		if (string == null) {
			return null;
		}
		try {
			return Duration.parse(string);
		} catch (final DateTimeParseException e) {
			return null;
		}
	}

	public static class PostLoadChecker implements Reloadable {

		private final ConfigurationHandler config;
		private final AbstractLogger logger;

		public PostLoadChecker(final ConfigurationHandler config, final AbstractLogger logger) {
			this.config = config;
			this.logger = logger;
		}

		@Override
		public void unload() {

		}

		@Override
		public void load() {
			if (!this.config.main().hasChild("api", "server-id")) {
				this.logger.warning("Your config file is missing the server-id option. If you upgraded from an older plugin version, it is recommended to delete main.yaml to let the plugin generate an up-to-date file.");
			}
		}
	}

}
