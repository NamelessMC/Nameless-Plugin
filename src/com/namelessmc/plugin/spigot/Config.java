package com.namelessmc.plugin.spigot;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.derkades.derkutils.FileUtils;

import java.io.File;
import java.io.IOException;

public enum Config {

	COMMANDS("commands.yml"),

	;

	private final @NotNull String fileName;

	private @Nullable FileConfiguration configuration;
	private final @NotNull File file;

	Config(final @NotNull String fileName){
		this.fileName = fileName;

		final File dataFolder = NamelessPlugin.getInstance().getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		this.file = new File(NamelessPlugin.getInstance().getDataFolder(), this.fileName);
	}

	public static void reloadAll() {
		final NamelessPlugin plugin = NamelessPlugin.getInstance();

		// Create config directory
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}

		// Create config files if missing
		for (final Config config : Config.values()) {
			config.reload();
		}
	}

	public @NotNull FileConfiguration getConfig() {
		if (this.configuration == null) {
			reload();
		}

		return this.configuration;
	}

	public void setConfig(final FileConfiguration config) {
		this.configuration = config;
	}

	public void reload() {
		if (!this.file.exists()) {
			try {
				FileUtils.copyOutOfJar(Config.class, "/" + this.fileName, this.file);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		this.configuration = YamlConfiguration.loadConfiguration(this.file);
	}

}
