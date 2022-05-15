package com.namelessmc.plugin.bukkit;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.modules.websend.WebsendCommand;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import net.md_5.bungee.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Websend implements Reloadable {

	private static final int MESSAGE_LENGTH_LIMIT = 2000;
	private static final int LINE_LENGTH_LIMIT = 500;

	private final @NonNull NamelessPlugin plugin;

	private @Nullable AbstractScheduledTask commandTask;
	private @Nullable AbstractScheduledTask logTask;
	private final Object logLock = new Object();
	private @Nullable List<String> logLines;

	private final Handler ourLogHandler = new Handler() {

		@Override
		public void publish(final LogRecord logRecord) {
			Objects.requireNonNull(logLines); // satisfy IDE
			synchronized (logLock) {
				String message = logRecord.getMessage();
				if (message.length() > MESSAGE_LENGTH_LIMIT) {
					Websend.this.plugin.logger().warning("Websend: not sending the previous log message, it is too long.");
					return;
				}
				String[] lines = message.split("\\r?\\n");
				for (String line : lines) {
					if (line.length() > LINE_LENGTH_LIMIT) {
						Websend.this.plugin.logger().warning("Websend: skipped a line in the previous log message, it is too long.");
						continue;
					}
					logLines.add(line);
				}
			}
		}

		@Override
		public void flush() {}

		@Override
		public void close() throws SecurityException {}

	};

	Websend(final @NonNull NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		// TODO also stop log task when the server shuts down, or everything will break
		// it's experimental so we don't care for now
		if (logTask != null) {
			Logger.getLogger("").removeHandler(ourLogHandler);
			logTask.cancel();
			logTask = null;
		}

		if (commandTask != null) {
			commandTask.cancel();
			commandTask = null;
		}

		final Configuration config = this.plugin.config().main();

		if (config.getBoolean("websend.command-executor.enabled")) {
			final Duration commandRate = Duration.parse(config.getString("websend.command-executor.interval"));
			this.commandTask = this.plugin.scheduler().runTimer(this::executeCommands, commandRate);
		}

		if (config.getBoolean("websend.console-capture.enabled")) {
			Logger.getLogger("").addHandler(ourLogHandler);
			this.plugin.logger().warning("Websend console capture enabled. This will probably break your server somehow.");
			final Duration logRate = Duration.parse(config.getString("websend.console-capture.send-interval"));
			this.logTask = this.plugin.scheduler().runTimer(this::sendLogLines, logRate);
		} else {
			logTask = null;
			logLines = null;
		}
	}

	void sendLogLines() {
		if (this.logLines == null || this.logLines.isEmpty()) {
			return;
		}

		this.plugin.scheduler().runAsync(() ->  {
			final List<String> linesToSend;
			synchronized (logLock) {
				linesToSend = new ArrayList<>(logLines);
				logLines.clear();
			}

			this.plugin.apiProvider().api().ifPresent(api -> {
				final Configuration config = this.plugin.config().main();
				int serverId = config.getInt("server-data-sender.server-id");
				if (serverId <= 0) {
					this.plugin.logger().warning("server-id is not configured");
					return;
				}
				try {
					api.websend().sendConsoleLog(serverId, linesToSend);
				} catch (NamelessException e) {
					this.plugin.logger().logException(e);
				}
			});
		});
	}

	private void executeCommands() {
		final Configuration config = this.plugin.config().main();
		final int serverId = config.getInt("server-data-sender.server-id");
		if (serverId <= 0) {
			this.plugin.logger().warning("Websend is enabled but 'server-data-sender.server-id' in config.yaml is not set properly.");
			return;
		}

		this.plugin.scheduler().runAsync(() -> {
			this.plugin.apiProvider().api().ifPresent(api -> {
				try {
					final List<WebsendCommand> commands = api.websend().getCommands(serverId);
					if (commands.isEmpty()) {
						return;
					}

					this.plugin.scheduler().runSync(() -> {
						for (final WebsendCommand command : commands) {
							try {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.getCommandLine());
							} catch (final CommandException e) {
								// continue executing other commands if one fails
								this.plugin.logger().logException(e);
							}
						}
					});
				} catch (NamelessException e) {
					this.plugin.logger().severe("Error retrieving websend commands");
					this.plugin.logger().logException(e);
				}
			});
		});
	}

}
