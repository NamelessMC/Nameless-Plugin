package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.AbstractYamlFile;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class YamlFileImpl extends AbstractYamlFile {

	private final @NotNull Configuration config;

	@Deprecated
	public YamlFileImpl(final Path path) {
		try {
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(path.toFile());
		} catch (final IOException e) {
			final RuntimeException e2 = new RuntimeException(e);
			e2.addSuppressed(e);
			throw e2;
		}
	}

	public YamlFileImpl(final @NotNull Configuration config) {
		this.config = config;
	}

	@Override
	public String getString(final String path) {
		return this.config.getString(path);
	}

	@Override
	public boolean isString(String path) {
		return this.config.contains(path);
	}

}
