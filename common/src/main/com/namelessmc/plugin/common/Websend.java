package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.websend.WebsendCommand;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Websend implements Reloadable {

	private static final int SEND_LOG_MAX_BYTES = 50_000;

	private final @NonNull NamelessPlugin plugin;
	private final @Nullable Path logPath;

	private @Nullable AbstractScheduledTask commandTask;
	private final Object commandLock = new Object();
	private @Nullable AbstractScheduledTask logTask;
	private final Object logLock = new Object();
	private int previousLogSize = 0;
	private boolean clearPrevious = true;

	Websend(final @NonNull NamelessPlugin plugin,
			final @Nullable Path logPath) {
		this.plugin = plugin;
		this.logPath = logPath;
	}

	@Override
	public void reload() {
		if (logTask != null) {
			logTask.cancel();
			logTask = null;
		}

		if (commandTask != null) {
			commandTask.cancel();
			commandTask = null;
		}

		final ConfigurationNode config = this.plugin.config().modules().node("websend");

		if (config.node("command-executor", "enabled").getBoolean()) {
			final Duration interval = ConfigurationHandler.getDuration(config.node("command-executor", "interval"));
			if (interval == null) {
				this.plugin.logger().warning("Websend command executor interval invalid");
				return;
			}
			this.commandTask = this.plugin.scheduler().runTimer(this::executeCommands, interval);
		}

		if (config.node("send-logs", "enabled").getBoolean()) {
			final Duration interval = ConfigurationHandler.getDuration(config.node("send-logs", "interval"));
			if (interval == null) {
				this.plugin.logger().warning("Websend send-logs interval invalid");
				return;
			}
			this.logTask = this.plugin.scheduler().runTimer(this::sendLogLines, interval);
		} else {
			logTask = null;
		}
	}

	private static String readToString(final Path path, final int start, final int length) throws IOException {
		try (final FileChannel channel = FileChannel.open(path)) {
			channel.position(start);
			final ByteBuffer buffer = ByteBuffer.allocate(length);
			while (buffer.hasRemaining()) {
				channel.read(buffer);
			}
			buffer.position(0); // Reset position, or resulting string will be empty!
			return StandardCharsets.UTF_8.decode(buffer).toString();
		}
	}

	void sendLogLines() {
		this.plugin.scheduler().runAsync(() ->  {
			synchronized (logLock) {
				try {
					final Path log = this.logPath;
					if (log == null) {
						this.plugin.logger().warning("Not sending logs, capturing logs not supported on your platform.");
						return;
					}

					if (!Files.isRegularFile(log)) {
						this.plugin.logger().warning("Log file does not exist or is not a regular file: " + log.toAbsolutePath());
						return;
					}

					final long newSizeLong = Files.size(log);
					if (newSizeLong > Integer.MAX_VALUE) {
						this.plugin.logger().warning("Log file is too large to read");
					}
					final int newSize = (int) newSizeLong;
					final int diff = newSize - this.previousLogSize;

					final int readStart;
					if (diff == 0) {
						// Nothing has been written to the log
						return;
					} else if (diff > SEND_LOG_MAX_BYTES) {
						// A lot of new data has been written to the log
						// Only read the last bit
						readStart = newSize - SEND_LOG_MAX_BYTES;
					} else if (diff > 0) {
						// Some new data has been written to the log
						readStart = this.previousLogSize;
					} else {
						// Log file got smaller, this likely means the server has rotated log files. Read the new log 
						// entirely or the last part if it's too long already. Ideally we'd try to find the compressed 
						// previous log and decompress it to send any lines written to the old log, but this is way too
						// much work.
						this.plugin.logger().info("Log file was rotated or deleted, Websend may have missed some lines written to the old log.");
						readStart = Math.max(0, newSize - SEND_LOG_MAX_BYTES);
					}

					final int readSize = newSize - readStart;

					final String logString = readToString(log, readStart, readSize);

					if (!logString.endsWith("\n")) {
						this.plugin.logger().info("Server is busy writing to the log file, trying again later");
						// Returning now means we never update this.previousLogSize, so next iteration it'll try to
						// read from the same starting point again.
						return;
					}

					final String[] split = logString.split("\n");
					final List<String> lines = new ArrayList<>(split.length);

					if (readSize == SEND_LOG_MAX_BYTES) {
						// Log was likely truncated
						lines.add(0, "[websend: skipped lines]");
						lines.addAll(Arrays.asList(split).subList(1, split.length));
					} else {
						lines.addAll(Arrays.asList(split));
					}

					final NamelessAPI api = this.plugin.apiProvider().api();
					if (api == null) {
						return;
					}

					final int serverId = this.plugin.config().main().node("server-data-sender", "server-id").getInt(0);
					if (serverId <= 0) {
						this.plugin.logger().warning("server-id is not configured");
						return;
					}

					api.websend().sendConsoleLog(serverId, lines, clearPrevious);
					// Only the first time, signal to Websend module that it should clear the previous server log
					// This means when the server is restarted, the old log is removed.
					clearPrevious = false;

					this.previousLogSize = newSize;
				} catch (IOException e) {
					this.plugin.logger().warning("Encountered an exception while trying to read the log file");
					this.plugin.logger().logException(e);
				} catch (NamelessException e) {
					this.plugin.logger().warning("Encountered an exception while sending server logs to website");
					this.plugin.logger().logException(e);
				}
			}
		});
	}

	private void executeCommands() {
		final int serverId = this.plugin.config().main().node("server-data-sender", "server-id").getInt(0);
		if (serverId <= 0) {
			this.plugin.logger().warning("Websend is enabled but 'server-data-sender.server-id' in main.yaml is not set properly.");
			return;
		}

		this.plugin.scheduler().runAsync(() -> {
			final NamelessAPI api = this.plugin.apiProvider().api();
			if (api == null) {
				return;
			}

			synchronized (commandLock) {
				try {
					final List<WebsendCommand> commands = api.websend().commands(serverId);
					if (commands.isEmpty()) {
						return;
					}

					this.plugin.scheduler().runSync(() -> {
						final NamelessConsole console = this.plugin.audiences().console();
						for (final WebsendCommand command : commands) {
							try {
								console.dispatchCommand(command.command());
							} catch (final Exception e) {
								// continue executing other commands if one fails
								this.plugin.logger().logException(e);
							}
						}
					});
				} catch (NamelessException e) {
					this.plugin.logger().severe("Error retrieving websend commands");
					this.plugin.logger().logException(e);
				}
			}
		});
	}

}
