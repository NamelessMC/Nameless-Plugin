package com.namelessmc.plugin.NamelessSponge;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;
import com.namelessmc.NamelessAPI.NamelessAPI;
import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessSponge.event.PlayerLogin;
import com.namelessmc.plugin.NamelessSponge.event.PlayerQuit;
import com.namelessmc.plugin.NamelessSponge.permissions.LuckPerms;
import com.namelessmc.plugin.NamelessSponge.permissions.Permissions;
import ninja.leaping.configurate.ConfigurationNode;

@Plugin(id = "namelessmc", name = "Nameless Sponge", version = "Pre-2", description = "The Plugin for NamelessMC", url = "https://plugin.namelessmc.com/", authors = {"IsS127", "Derkades", "Samerton"})
public class NamelessPlugin {
	
	@Inject
	private Logger logger;

	@Inject
    private Game game;
	
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir = null;
	
	private static NamelessPlugin instance;

	public static URL baseApiURL;
	
	public static final Map<UUID, Long> LOGIN_TIME = new HashMap<>();

	public static Permissions permissions = null;
	public static PermissionService permissionsservice = null;
	public static EconomyService economy = null;
	
	public static boolean disabled = false;
	
	@Listener
    public void onServerInitialization(GamePreInitializationEvent  event) {
        instance = this;
        log(Level.INFO, "Initializing NamelessMC " + getClass().getAnnotation(Plugin.class).version());
        
        // Luckperms Check
        if(game.getPluginManager().isLoaded("luckperms")) {
        	permissions = new LuckPerms();
        }
        
        // Economy Service Initliazation
        Optional<EconomyService> eopt = game.getServiceManager().provide(EconomyService.class);
        if (eopt.isPresent()) {
        	System.out.println("economy works");
        	economy = eopt.get();
        }
        
        // Initialize Configuration
        
        try {
			Config.initialize();
		} catch (IOException e) {
			log(Level.ERROR, "Unable to load config.");
			e.printStackTrace();
			return;
		}
			
		if (!checkConnection()) return;
        
    }
	
	@Listener
    public void onServerStart(GameStartedServerEvent event) {
		if(disabled) return;
		// Everything has loaded, move on with registering listeners and commands.
		//registerCommands();
		game.getEventManager().registerListeners(this, new PlayerLogin());
		game.getEventManager().registerListeners(this, new PlayerQuit());
			
		// Start saving data files every 15 minutes
	    game.getScheduler().createTaskBuilder().execute(new SaveConfig()).interval(15, TimeUnit.MINUTES);
	    
		// Start group synchronization task
		ConfigurationNode config = Config.MAIN.getConfig();
		if (!(config.getNode("group-synchronization.sync-interval").getInt() <= 0)) {
			long interval = config.getNode("group-synchronization.sync-interval").getLong() * 20L; // TODO check if this right..
			game.getScheduler().createTaskBuilder().execute(() -> {
				for (Player player : game.getServer().getOnlinePlayers()) {
					syncGroup(player);
				}
			}).interval(interval, TimeUnit.SECONDS);
		}
		
		long uploadPeriod = config.getNode("server-data-upload-rate").getLong() * 20L;
		game.getScheduler().createTaskBuilder().execute(new ServerDataSender()).delay(uploadPeriod, TimeUnit.SECONDS).interval(uploadPeriod, TimeUnit.SECONDS);
		
		log(Level.INFO, "Successfully loaded!");
	}
	
	@Listener
	public void onPluginReload(GameReloadEvent event) {
		if(disabled) { if(!checkConnection()) return; disabled = false;}
		
		game.getScheduler().createTaskBuilder().execute(new ServerDataSender()).async();
		
		try {
			for (Config config : Config.values()) {
				if (config.autoSave()) config.saveConfig();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		log(Level.INFO, "Successfully reloaded!");
	}
	
	@Listener
	public void onServerDisabling(GameStoppingServerEvent event) {
		if(disabled) return;
		game.getScheduler().createTaskBuilder().execute(new ServerDataSender()).async();
		
		// Save all Configuration
		try {
			for (Config config : Config.values()) {
				if (config.autoSave()) config.saveConfig();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if(disabled) return;
		if (event.getService().equals(PermissionService.class)) {
			permissionsservice = (PermissionService) event.getNewProviderRegistration().getProvider();
		}
    }
	
	private boolean checkConnection() {
		ConfigurationNode config = Config.MAIN.getConfig();
		String url = config.getNode("api-url").getString();
		if (url.equals("")) {
			log(Level.WARNING, "No API URL set in the NamelessMC configuration. Nothing will work until you set the correct url.");
			disabled = true;
			return false; // Prevent registering of commands, listeners, etc.
		} else {
			try {
				baseApiURL = new URL(url);
			} catch (MalformedURLException e) {
				// There is an exception, so the connection was not successful.
				log(Level.WARNING, "Invalid API Url/Key. Nothing will work until you set the correct url.");
				log(Level.ERROR, e.getMessage());
				disabled = true;
				return false; // Prevent registering of commands, listeners, etc.
			}

			Exception exception = NamelessAPI.checkWebAPIConnection(baseApiURL);
			if (exception != null) {
				// There is an exception, so the connection was unsuccessful.
				log(Level.WARNING, "Invalid API Url/Key. Nothing will work until you set the correct url.");
				log(Level.ERROR, exception.getMessage());
				disabled = true;
				return false; // Prevent registering of commands, listeners, etc.
			}
		}
		disabled = true;
		return true;
	}
	
	public static NamelessPlugin getInstance() {
		return instance;
	}
	
	public static Game getGame(){
		return NamelessPlugin.getInstance().game;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public static void log(Level level, String message) {
		Logger logger = NamelessPlugin.getInstance().getLogger();
		switch (level) {
			case DEBUG:
				logger.debug(message);
				break;
			case TRACE:
				logger.trace(message);
				break;
			case INFO:
				logger.info(message);
				break;
			case WARNING:
				logger.warn(message);
				break;
			case ERROR:
				logger.error(message);
				break;
			default:
				break;
		}
	}
	
	public static Path getDirectory() {
		return NamelessPlugin.getInstance().configDir;
	}
	
	public static void syncGroup(Player player) {
		ConfigurationNode config = Config.MAIN.getConfig();
		if (config.getNode("group-synchronization").getBoolean()) {
			ConfigurationNode permissionConfig = Config.MAIN.getConfig();
			for (Object object : permissionConfig.getNode("permissions").getChildrenMap().keySet()) {
				String groupID = (String) object;
				NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), NamelessPlugin.baseApiURL);
				if (String.valueOf(namelessPlayer.getGroupID()).equals(groupID)) {
					return;
				} else if (player.hasPermission(Permission.toGroupSyncPermission(permissionConfig.getString("permissions." + groupID)))) {
					Integer previousgroup = namelessPlayer.getGroupID();
					Text successPlayerMessage = Message.GROUP_SYNC_PLAYER_ERROR.getMessage();
					try {
						namelessPlayer.setGroup(Integer.parseInt(groupID));
						NamelessPlugin.log(Level.INFO, "Successfully changed " + player.getName() + "'s &agroup from " + previousgroup + " to " + groupID);
						player.sendMessage(successPlayerMessage);
					} catch (NumberFormatException e) {
						NamelessPlugin.log(Level.WARNING, "The Group ID is not a number.");
					} catch (NamelessException e) {
						Text errorPlayerMessage = Chat.toText(Message.GROUP_SYNC_PLAYER_ERROR.getMessageAsString().replace("%error%", e.getMessage()));
						NamelessPlugin.log(Level.WARNING, "Error changing &c" + player.getName() + "'s group: " + e.getMessage());
						player.sendMessage(errorPlayerMessage);
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public enum Level {
		DEBUG,
		TRACE,
		INFO,
		WARNING,
		ERROR;
	}
	
	public static class SaveConfig implements Runnable {

		@Override
		public void run() {
			getGame().getScheduler().createAsyncExecutor(getInstance()).execute(() -> {
				try {
					for (Config config : Config.values()) {
						if (config.autoSave())
							config.saveConfig();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

	}
}