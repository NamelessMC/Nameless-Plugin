package com.namelessmc.namelessplugin.sponge;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
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
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "namelessplugin", name = "Nameless Plugin", version = "1.0-SNAPSHOT")
public class NamelessPlugin {

	private static NamelessPlugin instance;
	
	public static NamelessPlugin getInstance(){
		return instance;
	}

	CommandManager cmdManager = Sponge.getCommandManager();

	/*
	 *  API URL
	 */
	private String apiURL = "";
	public boolean hasSetUrl = true;
	
	
	/*
	 *  NameLessMC permission string.
	 */
	
	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";
	
	/*
	 * Metrics
	 */
	Metrics metrics;

	private CommentedConfigurationNode config;

	@Inject
	@ConfigDir(sharedRoot = false)
	private File configDir;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private File configFile;

	@Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;

	@Inject
	private Logger logger;

	public Logger getLogger() {
		return logger;
	}

	public String getName() {
		return ((Plugin) this).name();
	}

	public String getVersion() {
		return ((Plugin) this).version();
	}

	public String getAPIUrl() {
		return apiURL;
	}


	@Listener
	public void onInitialize(GameInitializationEvent event) throws Exception {
		initConfig();
		registerListeners();
	}

	public void initConfig() throws Exception {
		if (!configDir.exists()) {
			configDir.mkdir();
		} else {
			config = configLoader.load();
		}
		if (!configFile.exists()){
			configFile.createNewFile();
			config = configLoader.load();

			config.getNode("api-url").setComment("This needs to be the full API URL, including API key").setValue("");
			config.getNode("enable-report").setComment("Enable report command?").setValue(true);
			configLoader.save(config);
		} else {
			config = configLoader.load();
		}
	}

	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
		// Register Metrics
		try {
			metrics = new Metrics(this);
			metrics.start();
			getLogger().info(Text.builder("Metrics Started!").color(TextColors.AQUA).build().toString());
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
		if (config.getNode("enable-reports").getBoolean()){
			CommandSpec reportCMD = CommandSpec.builder()
					.description(Text.of("Report Command"))
					.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
					.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("reason"))))
					.executor(new ReportCommand())
					.build();
			cmdManager.register(this, reportCMD, "report");
		} else {
			return;
		}
	}

}