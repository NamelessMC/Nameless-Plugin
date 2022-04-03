package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class NamelessPlugin extends Plugin implements CommonObjectsProvider {

	private static NamelessPlugin instance;
	public static NamelessPlugin getInstance() { return instance; }

	private Configuration config;
	public Configuration getConfig() { return this.config; }

	private AbstractYamlFile commandsConfig;
	@Override public AbstractYamlFile getCommandsConfig() { return this.commandsConfig; }

	private LanguageHandler language;
	@Override public LanguageHandler getLanguage() { return this.language; }

	private ApiProvider apiProvider;
	@Override public ApiProvider getApiProvider() { return this.apiProvider; }

	private ExceptionLogger exceptionLogger;
	@Override public ExceptionLogger getExceptionLogger() { return this.exceptionLogger; }

	private BungeeAudiences adventure;
	public AudienceProvider adventure() { return this.adventure; }

	private final @NotNull AbstractScheduler scheduler = new AbstractScheduler() {
		@Override
		public void runAsync(final Runnable runnable) {
			runnable.run();
		}

		@Override
		public void runSync(final Runnable runnable) {
			runnable.run();
		}
	};
	@Override public AbstractScheduler getScheduler() { return this.scheduler; }

	private ScheduledTask dataSenderTask;

	@Override
	public void onEnable() {
		instance = this;

		this.adventure = BungeeAudiences.create(this);
		this.language = new LanguageHandler(this.getLogger(), getDataFolder().toPath().resolve("languages"));

		try {
			reload();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Configuration copyFromJarAndLoad(Path dataFolder, String name) throws IOException {
		Path path = dataFolder.resolve(name);
		if (!Files.isRegularFile(path)) {
			try (InputStream in = getResourceAsStream(name)) {
				Files.copy(in, path);
			}
		}

		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(path.toFile());
	}

	public void reload() throws IOException {
		final Path dataFolder = getDataFolder().toPath();
		Files.createDirectories(dataFolder);

		this.config = copyFromJarAndLoad(dataFolder, "config.yml");
		this.commandsConfig = new YamlFileImpl(copyFromJarAndLoad(dataFolder, "commands.yml"));

		this.exceptionLogger = new ExceptionLogger(this.getLogger(), this.getConfig().getBoolean("single-line-exceptions"));

		this.apiProvider = new ApiProvider(
				this.getLogger(),
				this.getExceptionLogger(),
				getConfig().getString("api.url"),
				getConfig().getString("api.key"),
				getConfig().getBoolean("api.debug", false),
				getConfig().getBoolean("api.usernames", false),
				getConfig().getInt("api.timeout", 5000),
				getConfig().getBoolean("api.bypass-version-check"));

		try {
			this.getLanguage().updateFiles();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (!this.getLanguage().setActiveLanguage(
				this.config.getString("language", LanguageHandler.DEFAULT_LANGUAGE), YamlFileImpl::new)) {
			this.getLogger().severe("LANGUAGE FILE FAILED TO LOAD");
			this.getLogger().severe("THIS IS BAD NEWS, THE PLUGIN WILL BREAK");
			this.getLogger().severe("FIX IMMEDIATELY");
			this.getLogger().severe("In config.yml, set 'language' to '" + LanguageHandler.DEFAULT_LANGUAGE
					+ "' or any other supported language.");
			throw new RuntimeException("Failed to load language file");
		}

		if (this.dataSenderTask != null) {
			this.dataSenderTask.cancel();
			this.dataSenderTask = null;
		}

		final int rate = this.getConfig().getInt("server-data-upload-rate", 10);
		final int serverId = getConfig().getInt("server-id");
		if (rate >= 0 && serverId > 0) {
			this.dataSenderTask = getProxy().getScheduler().schedule(this, new ServerDataSender(), rate, rate, TimeUnit.SECONDS);
		}

		this.registerCommands();
	}

	private void registerCommands() {
		this.getProxy().getPluginManager().unregisterCommands(this);

		CommonCommand.getEnabledCommands(this).forEach(command -> {
			final String name = command.getActualName();
			final String permission = command.getPermission().toString();

			Command bungeeCommand = new Command(name, permission) {
				@Override
				public void execute(final CommandSender commandSender, final String[] args) {
					final BungeeCommandSender bungeeCommandSender = new BungeeCommandSender(commandSender);
					command.execute(bungeeCommandSender, args);
				}
			};

			this.getProxy().getPluginManager().registerCommand(this, bungeeCommand);
		});
	}

}
