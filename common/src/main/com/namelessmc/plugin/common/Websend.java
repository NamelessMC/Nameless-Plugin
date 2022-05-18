package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
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
import java.util.Optional;

public class Websend implements Reloadable {

	private static final int SEND_LOG_MAX_BYTES = 50_000;

	private final @NonNull NamelessPlugin plugin;
	private final @Nullable Path logPath;

	private @Nullable AbstractScheduledTask commandTask;
	private @Nullable AbstractScheduledTask logTask;
	private int previousLogSize = 0;

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

		if (config.node("console-capture", "enabled").getBoolean()) {
			this.plugin.logger().warning("Websend console capture enabled. This is an experimental feature!");
			final Duration interval = ConfigurationHandler.getDuration(config.node("console-capture", "interval"));
			if (interval == null) {
				this.plugin.logger().warning("Websend console capture interval invalid");
				return;
			}
			this.logTask = this.plugin.scheduler().runTimer(this::sendLogLines, interval);
		} else {
			logTask = null;
		}
	}

	void sendLogLines() {
		this.plugin.scheduler().runAsync(() ->  {
			try {
				final Path log = this.logPath;
				if (log == null) {
					this.plugin.logger().warning("Not sending logs, capturing logs not supported on your platform.");
					return;
				}

				if (!Files.isRegularFile(log)) {
					this.plugin.logger().warning("Log file does not exist or is not a regular file");
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
					// Log file got smaller, this likely means the server has
					// rotated log files. Read the log entirely or the last part
					// if it's too long again.
					readStart = Math.max(0, newSize - SEND_LOG_MAX_BYTES);
				}

				final int readSize = newSize - readStart;

				final String logString;

				try (final FileChannel channel = FileChannel.open(logPath)) {
					channel.position(readStart);
					final ByteBuffer buffer = ByteBuffer.allocate(readSize);
					channel.read(new ByteBuffer[]{buffer}, 0, 1);
					logString = StandardCharsets.UTF_8.decode(buffer).toString();
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

				final Optional<NamelessAPI> apiOptional = this.plugin.apiProvider().api();
				if (apiOptional.isEmpty()) {
					return;
				}

				final int serverId = this.plugin.config().main().node("server-data-sender", "server-id").getInt(0);
				if (serverId <= 0) {
					this.plugin.logger().warning("server-id is not configured");
					return;
				}

				apiOptional.get().websend().sendConsoleLog(serverId, lines);

				this.previousLogSize = newSize;
			} catch (IOException e) {
				this.plugin.logger().warning("Encountered an exception while trying to read the log file");
				this.plugin.logger().logException(e);
			} catch (NamelessException e) {
				this.plugin.logger().warning("Encountered an exception while sending server logs to website");
				this.plugin.logger().logException(e);
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
			this.plugin.apiProvider().api().ifPresent(api -> {
				try {
					final List<WebsendCommand> commands = api.websend().getCommands(serverId);
					if (commands.isEmpty()) {
						return;
					}

					this.plugin.scheduler().runSync(() -> {
						final NamelessConsole console = this.plugin.audiences().console();
						for (final WebsendCommand command : commands) {
							try {
								console.dispatchCommand(command.getCommandLine());
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
			});
		});
	}

}
