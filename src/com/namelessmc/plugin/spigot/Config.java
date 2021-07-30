package com.namelessmc.plugin.spigot;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import xyz.derkades.derkutils.FileUtils;

public enum Config {

	MAIN("config.yml", true, false),
	COMMANDS("commands.yml", true, false),

	;

	private String fileName;
	private boolean copyFromJar;
	private boolean autoSave;

	private FileConfiguration configuration;
	private File file;

	Config(final String fileName, final boolean copyFromJar, final boolean autoSave){
		this.fileName = fileName;
		this.copyFromJar = copyFromJar;
		this.autoSave = autoSave;

		final File dataFolder = NamelessPlugin.getInstance().getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		this.file = new File(NamelessPlugin.getInstance().getDataFolder(), this.fileName);
	}

	public static void initialize() throws IOException {
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

	public FileConfiguration getConfig() {
		if (this.configuration == null) {
			reload();
		}

		return this.configuration;
	}

	public void setConfig(final FileConfiguration config) {
		this.configuration = config;
	}

	public boolean autoSave() {
		return this.autoSave;
	}

	public void reload() {
		if (!this.file.exists()) {
			try {
				if (this.copyFromJar) {
					FileUtils.copyOutOfJar(Config.class, "/" + this.fileName, this.file);
				} else {
					this.file.createNewFile(); //Create blank file
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		this.configuration = YamlConfiguration.loadConfiguration(this.file);
	}

	public void save() {
		if (this.configuration != null) {
			try {
				this.configuration.save(this.file);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}