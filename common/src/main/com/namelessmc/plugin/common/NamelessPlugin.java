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

	private final List<List<Reloadable>> reloadables = List.of(
			new ArrayList<>(),
			new ArrayList<>(),
			new ArrayList<>()
	);
	private final List<AbstractPermissions> permissionAdapters = new ArrayList<>();
	private @Nullable AbstractPermissions chosenPermissionAdapter;

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
		this.registerReloadable(new GroupSync(this));

		this.registerPermissionAdapter(new LuckPermsPermissions());

		// Permission adapter is used by other reloadables, so must be loaded first.
		this.registerReloadable(new PermissionAdapterSelector(), Reloadable.Order.FIRST);

		int javaVer = Runtime.version().feature();
		if (javaVer > 11 && javaVer < 17) {
			this.logger.warning("You are running Java version " + javaVer + " which is non-LTS and no longer receives bug fixes or security fixes. Please update to Java 17.");
		}

		if (!this.config().main().hasChild("api", "server-id")) {
			this.logger.warning("Your config file is missing the server-id option. Please move it from the 'server-data-sender' section to the 'api' section, if you upgraded from an older version.");
		}
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

	public EventBus<NamelessEvent> events() {
		return this.eventBus;
	}

	public @Nullable AbstractPermissions permissions() {
		return this.chosenPermissionAdapter;
	}

	public void setAudienceProvider(final @NonNull AbstractAudienceProvider audienceProvider) {
		this.audienceProvider = audienceProvider;
	}

	public void unload() {
		for (Reloadable.Order order : Reloadable.Order.values()) {
			for (Reloadable reloadable : reloadables.get(order.ordinal())) {
				this.logger.fine(() -> "Unloading " + order + ": " + reloadable.getClass().getSimpleName());
				reloadable.unload();
			}
		}
	}

	public void load() {
		for (Reloadable.Order order : Reloadable.Order.values()) {
			for (Reloadable reloadable : reloadables.get(order.ordinal())) {
				this.logger.fine(() -> "Loading " + order + ": " + reloadable.getClass().getSimpleName());
				reloadable.load();
			}
		}
	}

	public <T extends Reloadable> T registerReloadable(T reloadable) {
		return this.registerReloadable(reloadable, Reloadable.Order.NORMAL);
	}

	public <T extends Reloadable> T registerReloadable(T reloadable, Reloadable.Order order) {
		this.reloadables.get(order.ordinal()).add(reloadable);
		return reloadable;
	}

	public void unregisterReloadable(Class<?> clazz) {
		for (Reloadable.Order order : Reloadable.Order.values()) {
			reloadables.get(order.ordinal()).removeIf(reloadable -> reloadable.getClass().equals(clazz));
		}
	}

	public <T extends AbstractPermissions> T registerPermissionAdapter(T adapter) {
		this.logger.fine(() -> "Registered permission adapter: " + adapter.getClass().getSimpleName());
		this.permissionAdapters.add(adapter);
		return adapter;
	}

	private class PermissionAdapterSelector implements Reloadable {

		@Override
		public void unload() {
			NamelessPlugin.this.chosenPermissionAdapter = null;

			for (AbstractPermissions permissions : NamelessPlugin.this.permissionAdapters) {
				permissions.unload();
			}
		}

		@Override
		public void load() {
			for (int i = NamelessPlugin.this.permissionAdapters.size() - 1; i >= 0; i--) {
				AbstractPermissions permissions = NamelessPlugin.this.permissionAdapters.get(i);
				// Permission adapters implement Reloadable but are not reloaded by the plugin's main reload system.
				// We need to reload it manually here.
				NamelessPlugin.this.logger.fine(() -> "Reloading permissions: " + permissions.getClass().getSimpleName());
				permissions.load();
				if (permissions.isUsable()) {
					NamelessPlugin.this.logger.fine(() -> "Chosen permission adapter: " + permissions.getClass().getSimpleName());
					NamelessPlugin.this.chosenPermissionAdapter = permissions;
					break;
				}
			}

			if (NamelessPlugin.this.chosenPermissionAdapter == null) {
				NamelessPlugin.this.logger.fine("Found no usable permission adapter");
			}
		}
	}

}
