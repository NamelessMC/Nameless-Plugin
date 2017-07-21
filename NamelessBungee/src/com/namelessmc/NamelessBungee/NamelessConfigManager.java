package com.namelessmc.NamelessBungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessMessages;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class NamelessConfigManager {

	NamelessPlugin plugin;

	private File file;
	private Configuration config;

	public NamelessConfigManager(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public void initializeFiles() {
		createDirs();
		initConfig();
		if (plugin.hasSetUrl()) {
			initCommands();
			initMessages();

			config = getConfig();

			// Use group & username synchronization
			if (config.getBoolean("update-username")) {
				initPlayersData();
			}

			if (config.getBoolean("group-synchronization")) {
				initPermissionHandler();
			}
		}
	}

	private void createDirs() {
		try {
			if (!plugin.getDataFolder().exists()) {
				// Folder within plugins doesn't exist, create one now...
				plugin.getDataFolder().mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Initialise configuration
	 */
	private void initConfig() {
		// Check config exists, if not create one
		try {

			file = new File(plugin.getDataFolder() + File.separator + "Config.yml");

			if (!file.exists()) {
				try (InputStream in = plugin.getResourceAsStream("Config.yml")) {
					// NamelessConfigs doesn't exist, create one now...
					NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO,
							"&aCreating NamelessMC configuration file...");
					Files.copy(in, file.toPath());

					NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
							"&4NamelessMC Config needs configuring, disabling features...");
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if(file.exists()){
				config = getConfig();

				plugin.setAPIUrl(config.getString("api-url"));

				if (plugin.getAPIUrl().isEmpty()) {
					// API URL not set
					NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
							"&4No API URL set in the NamelessMC configuration, disabling features.");
					plugin.setHasSetUrl(false);
				} else if (plugin.getAPI().checkConnection().hasError()) {
					NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
							"&4Invalid API Url/Key, Please set the correct API url! &cdisabling features");
					plugin.setHasSetUrl(false);
				} else {
					// Exists already, load it
					NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO,
							"&aLoaded NamelessMC configuration file...");
					plugin.setHasSetUrl(true);
				}
			}

		} catch (Exception e) {
			// Exception generated
			e.printStackTrace();
		}
	}

	/*
	 * Initialise the Player Info File
	 */
	private void initPlayersData() {
		file = new File(plugin.getDataFolder() + File.separator + "PlayersData.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Players Data File!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if(file.exists()){
			NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded Players Data File!");
		}
	}

	/*
	 * Initialize the Permissions NamelessConfigs.
	 */
	private void initPermissionHandler() {

		file = new File(plugin.getDataFolder(), "GroupSyncPermissions.yml");

		if (!file.exists()) {
			try (InputStream pConfig = plugin.getClass().getClassLoader()
					.getResourceAsStream("GroupSyncPermissions.yml")) {
				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO,
						"&aCreated Group Sync Permissions file!");
				Files.copy(pConfig, file.getAbsoluteFile().toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(file.exists()){
			NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded Group Sync Permissions file!");
		}
	}

	/*
	 * Initialize the Permissions NamelessConfigs.
	 */
	private void initMessages() {
		file = new File(plugin.getDataFolder(), "Messages.yml");

		if (!file.exists()) {
			try (InputStream mConfig = plugin.getClass().getClassLoader().getResourceAsStream("Messages.yml");) {

				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Messages file!");
				Files.copy(mConfig, file.getAbsoluteFile().toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(file.exists()){
			NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded Messages file!");
		}

	}

	/*
	 * Initialize the Commands NamelessConfigs.
	 */
	private void initCommands() {
		file = new File(plugin.getDataFolder(), "Commands.yml");

		if (!file.exists()) {
			try (InputStream cConfig = plugin.getClass().getClassLoader().getResourceAsStream("Commands.yml");) {

				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Commands file!");
				Files.copy(cConfig, file.getAbsoluteFile().toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if(file.exists()){
			NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded The Commands file!");
		}

	}

	public Configuration getConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "Config.yml");
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	public Configuration getPlayerDataConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "PlayersData.yml");
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return config;
	}

	public Configuration getGroupSyncPermissionsConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "GroupSyncPermissions.yml");
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	public Configuration getMessageConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "Messages.yml");
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	public Configuration getCommandsConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "Commands.yml");
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}
	
	public boolean contains(Configuration file, String contain){
		return file.get(contain, null) != null;
	}

}