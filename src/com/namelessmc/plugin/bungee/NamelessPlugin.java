package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.ApiProvider;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import com.namelessmc.plugin.common.logger.JulLogger;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class NamelessPlugin extends Plugin implements CommonObjectsProvider {

	private static NamelessPlugin instance;
	public static NamelessPlugin getInstance() { return instance; }

	private ConfigurationHandler configuration;
	@Override public ConfigurationHandler getConfiguration() { return this.configuration; }

	private AbstractLogger commonLogger;
	@Override public AbstractLogger getCommonLogger() { return this.commonLogger; }

	private LanguageHandler language;
	@Override public LanguageHandler getLanguage() { return this.language; }

	private ApiProvider apiProvider;
	@Override public ApiProvider getApiProvider() { return this.apiProvider; }

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
	@Override public @NotNull AbstractScheduler getScheduler() { return this.scheduler; }

	private ScheduledTask dataSenderTask;

	@Override
	public void onEnable() {
		instance = this;

		this.adventure = BungeeAudiences.create(this);

		reload();
	}

	public void reload() {
		final Path dataDirectory = getDataFolder().toPath();

		this.configuration = new ConfigurationHandler(dataDirectory);
		this.commonLogger = new JulLogger(this, this.getLogger());
		this.language = new LanguageHandler(this, getDataFolder().toPath());
		this.apiProvider = new ApiProvider(this);

		if (this.dataSenderTask != null) {
			this.dataSenderTask.cancel();
			this.dataSenderTask = null;
		}

		final Configuration config = this.getConfiguration().getMainConfig();
		final int rate = config.getInt("server-data-upload-rate", 10);
		final int serverId = config.getInt("server-id");
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
