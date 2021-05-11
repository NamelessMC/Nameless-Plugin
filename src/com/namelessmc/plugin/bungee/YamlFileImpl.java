package com.namelessmc.plugin.bungee;

import java.io.IOException;
import java.nio.file.Path;

import com.namelessmc.plugin.common.AbstractYamlFile;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class YamlFileImpl extends AbstractYamlFile {

	private final Configuration config;

	public YamlFileImpl(final Path path) {
		try {
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(path.toFile());
		} catch (final IOException e) {
			final RuntimeException e2 = new RuntimeException(e);
			e2.addSuppressed(e);
			throw e2;
		}
	}

	@Override
	public String getString(final String path) {
		return this.config.getString(path);
	}

}
