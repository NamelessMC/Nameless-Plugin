package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.modules.websend.WebsendCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.logging.Logger;

public class WebsendCommandExecutor implements Runnable {

	@Override
	public void run() {
		NamelessPlugin inst = NamelessPlugin.getInstance();
		FileConfiguration config = inst.getConfig();
		Logger log = inst.getLogger();
		final int serverId = config.getInt("server-id");
		if (serverId <= 0) {
			log.warning("Websend is enabled but 'server-id' in config.yml is not set properly.");
			return;
		}

		Bukkit.getScheduler().runTaskAsynchronously(inst, () -> {
			inst.getNamelessApi().ifPresent(api -> {
				try {
					final List<WebsendCommand> commands = api.websend().getCommands(serverId);
					if (commands.isEmpty()) {
						return;
					}

					Bukkit.getScheduler().runTask(inst, () -> {
						for (final WebsendCommand command : commands) {
							try {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.getCommandLine());
							} catch (final CommandException e) {
								// continue executing other commands if one fails
								e.printStackTrace();
							}
						}
					});
				} catch (NamelessException e) {
					log.severe("Error retrieving websend commands");
					e.printStackTrace();
				}
			});
		});
	}

}
