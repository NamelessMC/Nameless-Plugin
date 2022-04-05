package com.namelessmc.plugin.sponge;

import com.google.inject.Inject;
import com.namelessmc.plugin.common.ApiProvider;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.command.CommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import com.namelessmc.plugin.common.logger.Slf4jLogger;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import net.kyori.adventure.text.serializer.spongeapi.SpongeComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Plugin(id="nameless-plugin", name="Nameless Plugin", version="test", description="test")
public class NamelessPlugin implements CommonObjectsProvider {

	private final AbstractScheduler scheduler;
	@Override public AbstractScheduler getScheduler() { return this.scheduler; }

	private LanguageHandler language;
	@Override public LanguageHandler getLanguage() { return this.language; }

	private ApiProvider apiProvider;
	@Override public ApiProvider getApiProvider() { return this.apiProvider; }

	private ConfigurationHandler configuration;
	@Override public ConfigurationHandler getConfiguration() { return this.configuration; }

	private AbstractLogger commonLogger;
	@Override public AbstractLogger getCommonLogger() { return this.commonLogger; }

	private static NamelessPlugin instance;

	@Inject
	private Logger logger;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private Path dataDirectory;

	private final SpongeAudiences adventure;

	private final ArrayList<CommandMapping> registeredCommands = new ArrayList<>();

	@Inject
	public NamelessPlugin(final SpongeAudiences adventure) {
		this.adventure = adventure;
		this.scheduler = new AbstractScheduler() {

			@Override
			public void runAsync(Runnable runnable) {
				Task.builder().async().execute(runnable);
			}

			@Override
			public void runSync(Runnable runnable) {
				Task.builder().execute(runnable);
			}

		};

		instance = this;
	}

	static NamelessPlugin getInstance() {
		return instance;
	}

	SpongeAudiences adventure() {
		return this.adventure;
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		this.reload();
	}

	private void reload() {
		this.configuration = new ConfigurationHandler(this.dataDirectory);
		this.commonLogger = new Slf4jLogger(this, this.logger);
		this.apiProvider = new ApiProvider(this);
		this.language = new LanguageHandler(this, this.dataDirectory);

		this.registerCommands();
	}

	private void registerCommands() {
		final CommandManager manager = Sponge.getCommandManager();

		for (CommandMapping mapping : this.registeredCommands) {
			manager.removeMapping(mapping);
		}
		this.registeredCommands.clear();

		CommonCommand.getEnabledCommands(this).forEach(command -> {
			final String permission = command.getPermission().toString();
			final SpongeComponentSerializer ser = SpongeComponentSerializer.get();
			final Text usage = ser.serialize(command.getUsage());
			final Text description = ser.serialize(command.getDescription());

			CommandCallable spongeCommand = new CommandCallable() {
				@Override
				public CommandResult process(CommandSource source, String arguments) throws CommandException {
					String[] args = arguments.split(" ");
					CommandSender sender = new SpongeCommandSender(source);
					command.execute(sender, args);
					return CommandResult.success();
				}

				@Override
				public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
					return Collections.emptyList();
				}

				@Override
				public boolean testPermission(CommandSource source) {
					return source.hasPermission(permission);
				}

				@Override
				public Optional<Text> getShortDescription(CommandSource source) {
					return Optional.of(description);
				}

				@Override
				public Optional<Text> getHelp(CommandSource source) {
					return Optional.empty();
				}

				@Override
				public Text getUsage(CommandSource source) {
					return usage;
				}
			};

			manager.register(this, spongeCommand, command.getActualName()).ifPresentOrElse(
					this.registeredCommands::add,
					() -> {
						logger.warn("Unable to register command: " + command.getActualName());
					}
			);
		});

		this.registeredCommands.trimToSize();
	}

}
