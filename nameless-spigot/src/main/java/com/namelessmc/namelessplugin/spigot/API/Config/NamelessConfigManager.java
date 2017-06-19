package com.namelessmc.namelessplugin.spigot.API.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bukkit.configuration.file.YamlConfiguration;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;

public class NamelessConfigManager {

	NamelessPlugin plugin;

	private File file;
	private YamlConfiguration yamlFile;

	public NamelessConfigManager(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	public void initializeFiles() {
		createDirs();
		initConfig();
		if (plugin.hasSetUrl()) {
			initCommands();
			initMessages();

			file = new File(plugin.getDataFolder() + File.separator + "Config.yml");
			yamlFile = YamlConfiguration.loadConfiguration(file);

			// Use group & username synchronization
			if (yamlFile.getBoolean("update-username")) {
				initPlayersData();
			}

			if (yamlFile.getBoolean("group-synchronization")) {
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
				try (InputStream in = plugin.getResource("Config.yml")) {
					// NamelessConfigs doesn't exist, create one now...
					NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aCreating NamelessMC configuration file...");
					Files.copy(in, file.toPath());

					NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
							"&4NamelessMC Config needs configuring, disabling features...");
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				file = new File(plugin.getDataFolder() + File.separator + "Config.yml");
				yamlFile = YamlConfiguration.loadConfiguration(file);

				plugin.setAPIUrl(yamlFile.getString("api-url"));

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
					NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded NamelessMC configuration file...");
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
		} else {
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
				NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aCreated Group Sync Permissions file!");
				Files.copy(pConfig, file.getAbsoluteFile().toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
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
		} else {
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
		} else {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_INFO, "&aLoaded The Commands!");
		}

	}

	public YamlConfiguration getConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "Config.yml");
		yamlFile = YamlConfiguration.loadConfiguration(file);

		return yamlFile;
	}

	public YamlConfiguration getPlayerDataConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "PlayersData.yml");
		yamlFile = YamlConfiguration.loadConfiguration(file);

		return yamlFile;
	}

	public YamlConfiguration getGroupSyncPermissionsConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "GroupSyncPermissions.yml");
		yamlFile = YamlConfiguration.loadConfiguration(file);

		return yamlFile;
	}

	public YamlConfiguration getMessageConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "Messages.yml");
		yamlFile = YamlConfiguration.loadConfiguration(file);

		return yamlFile;
	}

	public YamlConfiguration getCommandsConfig() {
		file = new File(plugin.getDataFolder() + File.separator + "Commands.yml");
		yamlFile = YamlConfiguration.loadConfiguration(file);

		return yamlFile;
	}

}
