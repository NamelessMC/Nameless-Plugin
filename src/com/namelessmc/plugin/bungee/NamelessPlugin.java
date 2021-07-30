package com.namelessmc.plugin.bungee;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.YamlFileImpl;

import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class NamelessPlugin extends Plugin implements CommonObjectsProvider {

	private static NamelessPlugin instance;

	private Configuration config;
	private ScheduledTask dataSenderTask;

	private ApiProviderImpl apiProvider;
	private LanguageHandler language;
	private BungeeAudiences adventure;

	@Override
	public void onEnable() {
		instance = this;

		this.adventure = BungeeAudiences.create(this);
		this.language = new LanguageHandler(getDataFolder().toPath().resolve("languages"));

		try {
			reload();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public AbstractScheduler getScheduler() {
		return new AbstractScheduler() {

			@Override
			public void runAsync(final Runnable runnable) {
				runnable.run();
			}

			@Override
			public void runSync(final Runnable runnable) {
				runnable.run();
			}

		};
	}

	public void reload() throws IOException {
		final Path dataFolder = getDataFolder().toPath();
		final Path configFile = dataFolder.resolve("config.yml");

		if (!Files.isRegularFile(configFile)) {
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, configFile);
			}
		}

		this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile.toFile());

		this.apiProvider.loadConfiguration(this.config);

		try {
			this.getLanguage().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (!this.getLanguage().setActiveLanguage(
				Config.MAIN.getConfig().getString("language", LanguageHandler.DEFAULT_LANGUAGE), YamlFileImpl::new)) {
			this.getLogger().severe("LANGUAGE FILE FAILED TO LOAD");
			this.getLogger().severe("THIS IS BAD NEWS, THE PLUGIN WILL BREAK");
			this.getLogger().severe("FIX IMMEDIATELY");
			this.getLogger().severe("In config.yml, set 'language' to '" + LanguageHandler.DEFAULT_LANGUAGE
					+ "' or any other supported language.");
			throw new RuntimeException("Failed to load language file");
		}

		this.dataSenderTask.cancel();

		final int rate = this.getConfig().getInt("server-data-upload-rate", 10);
		final int serverId = getConfig().getInt("server-id");
		if (rate < 0 || serverId < 0) {
			this.dataSenderTask = null;
		} else {
			this.dataSenderTask = getProxy().getScheduler().schedule(this, new ServerDataSender(), rate, rate, TimeUnit.SECONDS);
		}
	}

	@Override
	public Optional<NamelessAPI> getNamelessApi() {
		return this.apiProvider.getNamelessApi();
	}

	@Override
	public LanguageHandler getLanguage() {
		return this.language;
	}

	@Override
	public AudienceProvider adventure() {
		return this.adventure;
	}

	public Configuration getConfig() {
		return this.config;
	}

	public static NamelessPlugin getInstance() {
		return instance;
	}

}
