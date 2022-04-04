package com.namelessmc.plugin.spigot;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.modules.websend.WebsendCommand;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
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
			final AbstractLogger logger = NamelessPlugin.getInstance().getCommonLogger();
			synchronized (logLines) {
				String message = logRecord.getMessage();
				if (message.length() > MESSAGE_LENGTH_LIMIT) {
					logger.warning("Websend: not sending the previous log message, it is too long.");
					return;
				}
				String[] lines = message.split("\\r?\\n");
				for (String line : lines) {
					if (line.length() > LINE_LENGTH_LIMIT) {
						logger.warning("Websend: skipped a line in the previous log message, it is too long.");
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

	Websend() {
		final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();
		if (config.getBoolean("websend.command-executor.enabled")) {
			int commandRate = config.getInt("websend.command-executor.interval", -1);
			commandTask = Bukkit.getScheduler().runTaskTimer(NamelessPlugin.getInstance(),
					this::executeCommands, commandRate * 20L, commandRate * 20L);
		} else {
			commandTask = null;
		}

		if (config.getBoolean("websend.console-capture.enabled")) {
			Logger.getLogger("").addHandler(ourLogHandler);
			int logRate = config.getInt("websend.console-capture.send-interval", -1);
			logTask = Bukkit.getScheduler().runTaskTimerAsynchronously(NamelessPlugin.getInstance(),
					this::sendLogLines, logRate*20L, logRate*20L);
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
			final AbstractLogger logger = NamelessPlugin.getInstance().getCommonLogger();
			final Configuration config = NamelessPlugin.getInstance().getConfiguration().getMainConfig();
			int serverId = config.getInt("server-data-sender.server-id");
			if (serverId <= 0) {
				logger.warning("server-id is not configured");
				return;
			}
			try {
				api.websend().sendConsoleLog(serverId, linesToSend);
			} catch (NamelessException e) {
				logger.logException(e);
			}
		});
	}

	private void executeCommands() {
		final NamelessPlugin inst = NamelessPlugin.getInstance();
		final Configuration config = inst.getConfiguration().getMainConfig();
		final AbstractLogger logger = NamelessPlugin.getInstance().getCommonLogger();
		final int serverId = config.getInt("server-data-sender.server-id");
		if (serverId <= 0) {
			logger.warning("Websend is enabled but 'server-data-sender.server-id' in config.yaml is not set properly.");
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
								logger.logException(e);
							}
						}
					});
				} catch (NamelessException e) {
					logger.severe("Error retrieving websend commands");
					logger.logException(e);
				}
			});
		});
	}

}
