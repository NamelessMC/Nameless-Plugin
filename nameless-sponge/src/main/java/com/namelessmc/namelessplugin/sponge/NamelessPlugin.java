package com.namelessmc.namelessplugin.sponge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;
import com.namelessmc.namelessplugin.sponge.commands.GetUserCommand;
import com.namelessmc.namelessplugin.sponge.commands.RegisterCommand;
import com.namelessmc.namelessplugin.sponge.commands.ReportCommand;
import com.namelessmc.namelessplugin.sponge.mcstats.Metrics;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

/*
 *  Sponge Version by Lmmb74
 */

@Plugin(id = "namelessplugin", name = "Nameless Plugin", version = "1.0-SNAPSHOT")
public class NamelessPlugin{

	private static NamelessPlugin instance;
	
	public static NamelessPlugin getInstance(){
		return instance;
	}

	@Inject
	Game game;

	CommandManager cmdManager = Sponge.getCommandManager();

	/*
	 *  API URL
	 */
	private String apiURL = "";
	
	
	/*
	 *  NamelessMC permissions strings.
	 */
	
	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";
	
	/*
	 *  Metrics
	 */
	Metrics metrics;

	/*
	 *  Configuration
	 */
	private String directory;
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	private CommentedConfigurationNode configNode;

	@Inject
	private Logger logger;

	public Logger getLogger() {
		return logger;
	}

	public String getAPIUrl() {
		return apiURL;
	}

	public String getName() {
		return "Nameless Plugin";
	}

	public String getVersion() {
		return "1.0-SNAPSHOT";
	}

	public Game getGame(){
		return game;
	}

	public CommentedConfigurationNode getConfig(){
		return configNode;
	}

	@Listener
	public void onInitialize(GameInitializationEvent event) throws Exception {
		directory = Sponge.getGame().getConfigManager().getPluginConfig(this).getDirectory().toString();
		initConfig();
		apiURL = getConfig().getNode("api-url").getString();
		registerListeners();
	}

	/*
	 *  Configuration Initialization
	 */
	public void initConfig() throws IOException {
		
		File dir = new File(directory);
		if (!dir.exists()){
			dir.mkdirs();
		}
		
		File config = new File(directory + File.separator + "config.conf");
		if (!config.exists()){
			config.createNewFile();
			Files.copy(this.getClass().getResource("config.conf").openStream(),
					config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		configManager = HoconConfigurationLoader.builder().setPath(config.toPath()).build();
		configNode = configManager.load();
		
	}

	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
		// Register Metrics
		try {
			metrics = new Metrics(this);
			metrics.start();
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
					.arguments(GenericArguments.string(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("reason")))
					.executor(new ReportCommand())
					.build();
			cmdManager.register(this, reportCMD, "report");
		} else {
			return;
		}
	}

}