package com.namelessmc.namelessplugin.sponge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;
import com.namelessmc.namelessplugin.sponge.commands.GetUserCommand;
import com.namelessmc.namelessplugin.sponge.commands.RegisterCommand;
import com.namelessmc.namelessplugin.sponge.commands.ReportCommand;
import com.namelessmc.namelessplugin.sponge.mcstats.Metrics;
import com.namelessmc.namelessplugin.sponge.utils.PluginInfo;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

/*
 *  Sponge Version by Lmmb74
 */

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION)
public class NamelessPlugin {

	private static NamelessPlugin instance;
	CommandManager cmdManager = Sponge.getCommandManager();

	@Inject
	private Logger logger;

	@Inject
	Game game;

	/*
	 *  Metrics
	 */
	Metrics metrics;

	/*
	 *  API URL
	 */
	private String apiURL;

	/*
	 *  NamelessMC permissions strings.
	 */
	
	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";

	/*
	 *  Configuration
	 */
	private String directory;
	private ConfigurationLoader<ConfigurationNode> configManager;
	private ConfigurationNode configNode;

	public static NamelessPlugin getInstance(){
		return instance;
	}

	public Logger getLogger() {
		return this.logger;
	}

	public String getAPIUrl() {
		return this.apiURL;
	}

	public Game getGame(){
		return this.game;
	}

	public ConfigurationNode getConfig(){
		return this.configNode;
	}

	public void runTaskAsynchronously(Runnable task) {
		Sponge.getScheduler().createTaskBuilder().execute(task).async().submit(this);
	}

	public Server getServer(){
		return this.game.getServer();
	}

	@Listener
	public void onInitialize(GameInitializationEvent event) throws Exception {
		directory = Sponge.getGame().getConfigManager().getPluginConfig(this).getDirectory().toString();
		initConfig();
		apiURL = getConfig().getNode("api-url").getString();
		registerListeners();
	}

	@Listener
	public void onStop(GameStoppingEvent event) throws Exception {
		getGame().getEventManager().unregisterPluginListeners(this);
	}

	/*
	 *  Configuration Initialization
	 */
	public void initConfig() throws IOException {
		
		File dir = new File(directory);
		if (!dir.exists()){
			dir.mkdirs();
		}
		
		File config = new File(directory + File.separator + "config.yml");
		if (!config.exists()){
			config.createNewFile();
			Files.copy(this.getClass().getResource("config.yml").openStream(),
					config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		configManager = YAMLConfigurationLoader.builder().setPath(config.toPath()).build();
		configNode = configManager.load();

	}

	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
		// Register Metrics
		try {
			this.metrics = new Metrics(this);
			this.metrics.start();
			getLogger().info(Text.of(TextColors.AQUA, "Metrics Started!").toPlain());
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		// Register commands
		CommandSpec getuserCMD = CommandSpec.builder()
				.description(Text.of("GetUser Command"))
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
				.executor(new GetUserCommand())
				.build();
		CommandSpec registerCMD = CommandSpec.builder()
				.description(Text.of("Register Command"))
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("e-mail"))))
				.executor(new RegisterCommand())
				.build();
		cmdManager.register(this, getuserCMD, "getuser");
		cmdManager.register(this, registerCMD, "register");
		if (getConfig().getNode("enable-reports").getBoolean()){
			CommandSpec reportCMD = CommandSpec.builder()
					.description(Text.of("Report Command"))
					.arguments(GenericArguments.seq(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))), GenericArguments.remainingJoinedStrings(Text.of("reason"))))
					.executor(new ReportCommand())
					.build();
			cmdManager.register(this, reportCMD, "report");
		} else {
			return;
		}
	}

}