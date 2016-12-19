package com.namelessmc.namelessplugin.sponge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;
import com.namelessmc.namelessplugin.sponge.commands.GetNotificationsCommand;
import com.namelessmc.namelessplugin.sponge.commands.GetUserCommand;
import com.namelessmc.namelessplugin.sponge.commands.RegisterCommand;
import com.namelessmc.namelessplugin.sponge.commands.ReportCommand;
import com.namelessmc.namelessplugin.sponge.commands.SetGroupCommand;
import com.namelessmc.namelessplugin.sponge.mcstats.Metrics;
import com.namelessmc.namelessplugin.sponge.player.PlayerEventListener;
import com.namelessmc.namelessplugin.sponge.utils.PluginInfo;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

/*
 *  Sponge Version by Lmmb74
 */

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION)
public class NamelessPlugin {

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
	private String apiURL = "";
	public boolean hasSetUrl = true;

	/*
	 *  NamelessMC permissions strings.
	 */
	
	public final String permission = "namelessmc";
	public final String permissionAdmin = "namelessmc.admin";

	/*
	 *  Configuration
	 */
	private ConfigurationLoader<ConfigurationNode> configManager;
	private ConfigurationNode configNode;

	public Logger getLogger() {
		return logger;
	}

	public String getAPIUrl() {
		return apiURL;
	}

	public Game getGame(){
		return game;
	}

	public ConfigurationNode getConfig(){
		return configNode;
	}

	@Listener
	public void onInitialize(GamePreInitializationEvent event) throws Exception {
		getLogger().info("Initializing " + PluginInfo.NAME);
		initConfig();
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
		
		File config = new File(new File("config", "NamelessPlugin"), "config.yml");
		File dir = new File(config.getParent());

		if (!dir.exists()){
			dir.mkdirs();
		}

		if (!config.exists()){
			config.createNewFile();
			InputStream defaultConfig = getClass().getClassLoader().getResourceAsStream("config.yml");
			Files.copy(defaultConfig, config.getAbsoluteFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
		}

		configManager = YAMLConfigurationLoader.builder().setPath(config.toPath()).build();
		configNode = configManager.load();

		apiURL = configNode.getNode("api-url").getString();
		if (apiURL.isEmpty()) {
			hasSetUrl = false;
		}

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
		
		// Register commands if api url is set
		if (hasSetUrl){
			CommandSpec getuserCMD = CommandSpec.builder()
					.description(Text.of("GetUser Command"))
					.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
					.executor(new GetUserCommand(this))
					.build();
			CommandSpec registerCMD = CommandSpec.builder()
					.description(Text.of("Register Command"))
					.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("e-mail"))))
					.executor(new RegisterCommand(this))
					.build();
			CommandSpec getnotificationsCMD = CommandSpec.builder()
					.description(Text.of("GetNotifications Command"))
					.executor(new GetNotificationsCommand(this))
					.build();
			CommandSpec setgroupCMD = CommandSpec.builder()
					.description(Text.of("SetGroup Command"))
					.arguments(GenericArguments.seq(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))), GenericArguments.onlyOne(GenericArguments.string(Text.of("groupId")))))
					.executor(new SetGroupCommand(this))
					.build();
			cmdManager.register(this, getuserCMD, "getuser");
			cmdManager.register(this, registerCMD, "register");
			cmdManager.register(this, getnotificationsCMD, "getnotifications");
			cmdManager.register(this, setgroupCMD, "setgroup");
			if (getConfig().getNode("enable-reports").getBoolean()){
				CommandSpec reportCMD = CommandSpec.builder()
						.description(Text.of("Report Command"))
						.arguments(GenericArguments.seq(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))), GenericArguments.remainingJoinedStrings(Text.of("reason"))))
						.executor(new ReportCommand(this))
						.build();
				cmdManager.register(this, reportCMD, "report");
			} else {
				getLogger().info("Reports not enabled in config. Disabling...");
			}
		} else {
			getLogger().warn("API URL MUST BE SET IN ORDER TO USE THE PLUGIN!");
			getLogger().info("Disabling " + PluginInfo.NAME);
			return;
		}
		Sponge.getEventManager().registerListeners(this, new PlayerEventListener(this));
	}

	/*
	 *  Update username/group on login
	 */
	public boolean loginCheck(Player player){
		// Check when user last logged in, only update username and group if over x hours ago
		// TODO
		return true;
	}

}