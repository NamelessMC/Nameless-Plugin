package com.namelessmc.plugin.common;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.derkades.derkutils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationHandler implements Reloadable {

	private final @NonNull Path dataDirectory;
	private @Nullable Configuration mainConfig;
	private @Nullable Configuration commandsConfig;

	public ConfigurationHandler(final @NonNull Path dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public @NonNull Configuration main() {
		if (this.mainConfig == null) {
			throw new IllegalStateException("config requested before load");
		}
		return this.mainConfig;
	}

	public @NonNull Configuration commands() {
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

	private Configuration copyFromJarAndLoad(final @NonNull String name) throws IOException {
		Path path = dataDirectory.resolve(name);
		FileUtils.copyOutOfJar(ConfigurationHandler.class, name, path);
		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(path.toFile());
	}

}