package com.namelessmc.plugin.velocity;

import com.google.inject.Inject;
import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.platform.AudienceProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

@Plugin(id = "nameless-plugin",
		name = "Nameless Plugin",
		version = "@project.version@",
		url = "https://plugin.namelessmc.com/",
		description = "Integration with NamelessMC websites",
		authors = {"Derkades"})
public class NamelessPlugin implements CommonObjectsProvider {

	private AbstractScheduler scheduler;
	@Override public AbstractScheduler getScheduler() { return this.scheduler; }

	private LanguageHandler language;
	@Override public LanguageHandler getLanguage() { return this.language; }

	private ApiProvider apiProvider;
	@Override public ApiProvider getApiProvider() { return this.apiProvider; }

	private AudienceProvider adventure;
	@Override public AudienceProvider adventure() { return this.adventure; }

	private ExceptionLogger exceptionLogger;
	@Override public ExceptionLogger getExceptionLogger() { return this.exceptionLogger; }

	private AbstractYamlFile commandsConfig;
	@Override public AbstractYamlFile getCommandsConfig() { return this.commandsConfig; }

	private final @NotNull Yaml yaml = new Yaml();
	private MappingNode mainConfig;

	private final ArrayList<String> registeredCommands = new ArrayList<>();

	private final @NotNull ProxyServer server;
	private final @NotNull Logger logger;
	private final @NotNull Path dataDirectory;

	@Inject
	public NamelessPlugin(final @NotNull ProxyServer server,
						  final @NotNull Logger logger,
						  final @NotNull @DataDirectory Path dataDirectory) {
		this.server = server;
		this.logger = logger;
		this.dataDirectory = dataDirectory;
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		this.reload();
	}

	private MappingNode copyFromJarAndLoad(String name) throws IOException {
		Path path = this.dataDirectory.resolve(name);
		if (!Files.isRegularFile(path)) {
			try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(name)) {
				Files.copy(in, path);
			}
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			return (MappingNode) yaml.compose(reader);
		}
	}

	private void reload() {
		try {
			this.mainConfig = copyFromJarAndLoad("config.yml");
			this.commandsConfig = new YamlFileImpl(copyFromJarAndLoad("commands.yml"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.registerCommands();
	}

	private void registerCommands() {
		for (String registeredName : registeredCommands) {
			this.server.getCommandManager().unregister(registeredName);
		}
		registeredCommands.clear();

		CommonCommand.getEnabledCommands(this).forEach(command -> {
			final String permission = command.getPermission().toString();
			Command velocityCommand = new SimpleCommand() {
				@Override
				public void execute(Invocation invocation) {
					command.execute(new VelocityCommandSender(invocation.source()), invocation.arguments());
				}

				@Override
				public boolean hasPermission(final Invocation invocation) {
					return invocation.source().hasPermission(permission);
				}
			};
			String name = command.getActualName();
			this.server.getCommandManager().register(name, velocityCommand);
			this.registeredCommands.add(name);
		});

		this.registeredCommands.trimToSize();
	}

}
