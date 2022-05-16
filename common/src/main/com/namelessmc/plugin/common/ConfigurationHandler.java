package com.namelessmc.plugin.common;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import xyz.derkades.derkutils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationHandler implements Reloadable {

	private final @NonNull Path dataDirectory;
	private @Nullable CommentedConfigurationNode mainConfig;
	private @Nullable CommentedConfigurationNode commandsConfig;

	public ConfigurationHandler(final @NonNull Path dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public @NonNull CommentedConfigurationNode main() {
		if (this.mainConfig == null) {
			throw new IllegalStateException("config requested before load");
		}
		return this.mainConfig;
	}

	public @NonNull CommentedConfigurationNode commands() {
		if (this.commandsConfig == null) {
			throw new IllegalStateException("config requested before load");
		}
		return this.commandsConfig;
	}

	@Override
	public void reload() {
		try {
			Files.createDirectories(dataDirectory);
			this.mainConfig = copyFromJarAndLoad("config.yaml");
			this.commandsConfig = copyFromJarAndLoad("commands.yaml");
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CommentedConfigurationNode copyFromJarAndLoad(final @NonNull String name) throws IOException {
		Path path = dataDirectory.resolve(name);
		FileUtils.copyOutOfJar(ConfigurationHandler.class, name, path);
		return YamlConfigurationLoader.builder().path(path).build().load();
	}

}
