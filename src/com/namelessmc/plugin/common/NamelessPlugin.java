package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NamelessPlugin {

	private final AbstractScheduler scheduler;
	private final ConfigurationHandler configuration;
	private final AbstractLogger logger;
	private final ApiProvider api;
	private final LanguageHandler language;

	private final List<Reloadable> reloadables = new ArrayList<>();

	public NamelessPlugin(final @NotNull Path dataDirectory,
						  final @NotNull AbstractScheduler scheduler,
						  final @NotNull Function<ConfigurationHandler, AbstractLogger> loggerInstantiator) {
		this.scheduler = scheduler;

		this.configuration = this.registerReloadable(
				new ConfigurationHandler(dataDirectory)
		);
		this.logger = this.registerReloadable(
				loggerInstantiator.apply(this.configuration)
		);
		this.api = this.registerReloadable(
				new ApiProvider(scheduler, this.logger, this.configuration)
		);
		this.language = this.registerReloadable(
				new LanguageHandler(dataDirectory, this.configuration, this.logger)
		);
	}

	public ConfigurationHandler config() {
		return this.configuration;
	}

	public AbstractLogger logger() {
		return this.logger;
	}

	public ApiProvider api() {
		return this.api;
	}

	public LanguageHandler language() {
		return this.language;
	}

	public AbstractScheduler scheduler() {
		return this.scheduler;
	}

	public void reload() {
		for (Reloadable reloadable : reloadables) {
			reloadable.reload();
		}
	}

	public <T extends Reloadable> T registerReloadable(T reloadable) {
		this.reloadables.add(reloadable);
		return reloadable;
	}

}
