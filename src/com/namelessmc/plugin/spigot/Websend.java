package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.modules.websend.WebsendCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Websend {

	private static final int MESSAGE_LENGTH_LIMIT = 2000;
	private static final int LINE_LENGTH_LIMIT = 500;

	private final @Nullable BukkitTask commandTask;
	private final @Nullable BukkitTask logTask;
	private final @Nullable List<String> logLines;

	private final Handler ourLogHandler = new Handler() {
		@Override
		public void publish(LogRecord logRecord) {
			Objects.requireNonNull(logLines); // satisfy IDE
			synchronized (logLines) {
				String message = logRecord.getMessage();
				if (message.length() > MESSAGE_LENGTH_LIMIT) {
					NamelessPlugin.getInstance().getLogger().warning("Websend: not sending the previous log message, it is too long.");
					return;
				}
				String[] lines = message.split("\\r?\\n");
				for (String line : lines) {
					if (line.length() > LINE_LENGTH_LIMIT) {
						NamelessPlugin.getInstance().getLogger().warning("Websend: skipped a line in the previous log message, it is too long.");
						continue;
					}
					logLines.add(line);
				}
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {

		}
	};

	Websend(NamelessPlugin inst) {
		if (inst.getConfig().getBoolean("websend.command-executor.enabled")) {
			int commandRate = inst.getConfig().getInt("websend.command-executor.interval", -1);
			commandTask = Bukkit.getScheduler().runTaskTimer(inst, this::executeCommands, commandRate * 20L, commandRate * 20L);
		} else {
			commandTask = null;
		}

		if (inst.getConfig().getBoolean("websend.console-capture.enabled")) {
			Logger.getLogger("").addHandler(ourLogHandler);
			int logRate = inst.getConfig().getInt("websend.console-capture.send-interval", -1);
			logTask = Bukkit.getScheduler().runTaskTimerAsynchronously(inst, this::sendLogLines, logRate*20L, logRate*20L);
			logLines = new ArrayList<>();
		} else {
			logTask = null;
			logLines = null;
		}
	}

	void stop() {
		if (logTask != null) {
			Logger.getLogger("").removeHandler(ourLogHandler);
			logTask.cancel();
		}

		if (commandTask != null) {
			commandTask.cancel();
		}
	}

	void sendLogLines() {
		Objects.requireNonNull(logLines); // satisfy IDE

		if (this.logLines.isEmpty()) {
			return;
		}

		final List<String> linesToSend;
		synchronized (logLines) {
			linesToSend = new ArrayList<>(logLines);
			logLines.clear();
		}

		NamelessPlugin.getInstance().getNamelessApi().ifPresent(api -> {
			int serverId = NamelessPlugin.getInstance().getConfig().getInt("server-id");
			if (serverId <= 0) {
				NamelessPlugin.getInstance().getLogger().warning("server-id is not configured");
				return;
			}
			try {
				api.websend().sendConsoleLog(serverId, linesToSend);
			} catch (NamelessException e) {
				e.printStackTrace();
			}
		});
	}

	private void executeCommands() {
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
