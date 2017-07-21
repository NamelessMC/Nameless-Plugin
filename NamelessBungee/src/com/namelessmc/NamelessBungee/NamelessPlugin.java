package com.namelessmc.NamelessBungee;

import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.UpdateChecker;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessMessages;
import com.namelessmc.namelessplugin.bungeecord.commands.CommandWithArgs;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.GetNotificationsCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.GetUserCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.RegisterCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.ReportCommand;
import com.namelessmc.namelessplugin.bungeecord.commands.alone.SetGroupCommand;
import com.namelessmc.namelessplugin.bungeecord.player.PlayerEventListener;

import net.md_5.bungee.api.plugin.Plugin;

public class NamelessPlugin extends Plugin {

	private static NamelessPlugin instance;

	private String apiURL = "";
	private boolean hasSetUrl = false;

	public static final String permission = "namelessmc";
	public static final String permissionAdmin = "namelessmc.admin";

	@Override
	public void onEnable() {
		instance = this;

		// Register the API
		api = new NamelessAPI(this);

		// Init config files.
		api.getConfigManager().initializeFiles();

		if (hasSetUrl) {
			registerListeners();
		}
		if (getAPI().getConfigManager().getConfig().getBoolean("update-checker")) {
			checkForUpdate();
		} else {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, "&CIt is recommended to enable update checker.");
		}

	}

	public void registerListeners() {
		if (api.getConfigManager().getCommandsConfig().getBoolean("Commands.Alone.Use")
				&& api.getConfigManager().getCommandsConfig().getBoolean("Commands.SubCommand.Use")) {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING,
					"&4ERROR REGISTERING COMMANDS! BOUTH IS SET TO TRUE!");
		} else if (api.getConfigManager().getCommandsConfig().getBoolean("Commands.Alone.Use")) {
			String register = api.getConfigManager().getCommandsConfig().getString("Commands.Alone.Register");
			String getUser = api.getConfigManager().getCommandsConfig().getString("Commands.Alone.GetUser");
			String getNotifications = api.getConfigManager().getCommandsConfig()
					.getString("Commands.Alone.GetNotifications");
			String setGroup = api.getConfigManager().getCommandsConfig().getString("Commands.Alone.SetGroup");
			String report = api.getConfigManager().getCommandsConfig().getString("Commands.Alone.Report");
			if (api.getConfigManager().getConfig().getBoolean("enable-registration")) {
				getProxy().getPluginManager().registerCommand(this, new RegisterCommand(this, register));
			}
			getProxy().getPluginManager().registerCommand(this, new GetUserCommand(this, getUser));
			getProxy().getPluginManager().registerCommand(this, new GetNotificationsCommand(this, getNotifications));
			getProxy().getPluginManager().registerCommand(this, new SetGroupCommand(this, setGroup));
			if (api.getConfigManager().getConfig().getBoolean("enable-reports")) {
				getProxy().getPluginManager().registerCommand(this, new ReportCommand(this, report));
			}
		} else if (api.getConfigManager().getCommandsConfig().getBoolean("Commands.SubCommand.Use")) {
			String subCommand = api.getConfigManager().getCommandsConfig().getString("Commands.SubCommand.Main");
			getProxy().getPluginManager().registerCommand(this, new CommandWithArgs(this, subCommand));
		} else {
			NamelessChat.sendToLog(NamelessMessages.PREFIX_WARNING, "&4ERROR REGISTERING COMMANDS!");
		}

		// Register events
		getProxy().getPluginManager().registerListener(this, new PlayerEventListener(this));
	}
	
	public static NamelessPlugin getInstance() {
		return instance;
	}

}