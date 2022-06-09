package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.audiences.AbstractAudienceProvider;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.event.NamelessEvent;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.kyori.event.EventBus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
	private final DateFormatter dateFormatter;
	private final UserCache userCache;
	private final EventBus<NamelessEvent> eventBus;
	private final PropertiesManager propertiesManager;

	private final List<Reloadable> reloadables = new ArrayList<>();

	private AbstractAudienceProvider audienceProvider;

	public NamelessPlugin(final Path dataDirectory,
						  final AbstractScheduler scheduler,
						  final Function<ConfigurationHandler, AbstractLogger> loggerInstantiator,
						  final @Nullable Path logPath,
						  final String platformInternalName,
						  final String platformVersion) {
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
		this.propertiesManager = this.registerReloadable(
				new PropertiesManager(dataDirectory.resolve("nameless.dat"), this));
		this.language = this.registerReloadable(
				new LanguageHandler(dataDirectory, this.configuration, this.logger)
		);
		this.dateFormatter = this.registerReloadable(
				new DateFormatter(this.configuration));
		this.userCache = this.registerReloadable(
				new UserCache(this));

		this.eventBus = EventBus.create(NamelessEvent.class);

		this.registerReloadable(new AnnouncementTask(this));
		this.registerReloadable(new JoinNotificationsMessage(this));
		this.registerReloadable(new JoinNotRegisteredMessage(this));
		this.registerReloadable(new Metrics(this, platformInternalName, platformVersion));
		this.registerReloadable(new Store(this));
		this.registerReloadable(new SyncBanToWebsite(this));
		this.registerReloadable(new Websend(this, logPath));
	}

	public ConfigurationHandler config() {
		return this.configuration;
	}

	public AbstractLogger logger() {
		return this.logger;
	}

	public ApiProvider apiProvider() {
		return this.api;
	}

	public LanguageHandler language() {
		return this.language;
	}

	public DateFormatter dateFormatter() {
		return this.dateFormatter;
	}

	public AbstractScheduler scheduler() {
		return this.scheduler;
	}

	public AbstractAudienceProvider audiences() {
		return this.audienceProvider;
	}

	public UserCache userCache() {
		return this.userCache;
	}

	public @NonNull EventBus<NamelessEvent> events() {
		return this.eventBus;
	}

	public void setAudienceProvider(final @NonNull AbstractAudienceProvider audienceProvider) {
		this.audienceProvider = audienceProvider;
	}

	public PropertiesManager properties() {
		return this.propertiesManager;
	}

	public void reload() {
		for (Reloadable reloadable : reloadables) {
			this.logger.fine(() -> "Reloading: " + reloadable.getClass().getSimpleName());
			reloadable.reload();
		}
	}

	public <T extends Reloadable> T registerReloadable(T reloadable) {
		this.reloadables.add(reloadable);
		return reloadable;
	}

}
