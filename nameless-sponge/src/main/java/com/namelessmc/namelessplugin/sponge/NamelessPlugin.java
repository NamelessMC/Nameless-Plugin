package com.namelessmc.namelessplugin.sponge;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
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

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File configFile;
   
    @Inject
    @DefaultConfig(sharedRoot = true)
    ConfigurationLoader<CommentedConfigurationNode> configManager;
    
    @Inject
	CommentedConfigurationNode config;

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

	/*
	 *  Initialise configuration
	 */
	private void initConfig() throws Exception {
		if (!configFile.exists()) {
			configFile.createNewFile();
			configManager.load();
			config.setComment("Nameless Plugin configuration");
			config.setComment("");
			config.getNode("api-url")
			  .setComment("API URL")
			  .setComment("This needs to be the full API URL, including API key")
			  .setComment("For example http://yoursite.com/api/v1/API_KEY")
			  .setValue(apiURL);
			config.setComment("");
			config.getNode("enable-reports")
			  .setComment("Use reports?")
			  .setComment("If true, a /report command will be added to report users ingame")
			  .setComment("which will be added to the website's report system")
			  .setComment("Valid values = 'true', 'false'")
			  .setValue("true");
			configManager.save(config);
		} else {
			configManager.load();
		}
		if(apiURL.equals(config.getString("api-url"))){
			hasSetUrl = false;
		} else {
			hasSetUrl = true;
		}
	}

	/*
	 * Register Commands/Events
	 */
	public void registerListeners(){
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
		CommandSpec reportCMD = CommandSpec.builder()
			    .description(Text.of("Report Command"))
			    .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
			    .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("reason"))))
			    .executor(new ReportCommand())
			    .build();
		cmdManager.register(this, getuserCMD, "getuser");
		cmdManager.register(this, registerCMD, "register");
		cmdManager.register(this, reportCMD, "report");
	}

}