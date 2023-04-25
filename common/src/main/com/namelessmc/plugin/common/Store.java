package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.store.PendingCommandsResponse;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

public class Store implements Reloadable {

	private final NamelessPlugin plugin;

	private @Nullable AbstractScheduledTask commandTask;

	public Store(NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void unload() {
		if (this.commandTask != null) {
			this.commandTask.cancel();
			this.commandTask = null;
		}
	}

	@Override
	public void load() {
		ConfigurationNode commandExecutor = this.plugin.config().modules().node("store", "command-executor");
		if (!commandExecutor.node("enabled").getBoolean()) {
			return;
		}
		Duration interval = ConfigurationHandler.getDuration(commandExecutor.node("interval"));
		if (interval == null) {
			this.plugin.logger().warning("Invalid interval for store module command executor");
			return;
		}

		this.commandTask = this.plugin.scheduler().runTimer(this::retrievePendingCommands, interval);
	}

	public void retrievePendingCommands() {
		final int connectionId = this.plugin.config().modules().node("store", "connection-id").getInt();

		this.plugin.logger().fine("Store: Retrieving pending commands");

		this.plugin.scheduler().runAsync(() -> {
			NamelessAPI api = this.plugin.apiProvider().api();
			if (api == null) {
				this.plugin.logger().fine("Store: API is not available");
				return;
			}

			try {
				final PendingCommandsResponse pendingCommands = api.store().pendingCommands(connectionId);

				if (pendingCommands.customers().isEmpty()) {
					this.plugin.logger().fine("Store: nothing to do");
					return;
				}

				this.plugin.scheduler().runSync(() -> this.runPendingCommands(api, pendingCommands.shouldUseUuids(), pendingCommands.customers()));
			} catch (NamelessException e) {
				this.plugin.logger().logException(e);
			}
		});
	}

	public void runPendingCommands(NamelessAPI api,
								   boolean useUuids,
								   List<PendingCommandsResponse.PendingCommandsCustomer> customers) {
		Deque<PendingCommandsResponse.PendingCommand> completedCommands = new ArrayDeque<>();

		for (PendingCommandsResponse.PendingCommandsCustomer customer : customers) {
			this.plugin.logger().fine(() -> "Processing commands for customer: " + customer.username());

			NamelessPlayer player;
			if (useUuids) {
				player = this.plugin.audiences().player(customer.identifierAsUuid());
			} else {
				player = this.plugin.audiences().playerByUsername(customer.username());
			}

			for (PendingCommandsResponse.PendingCommand pendingCommand : customer.pendingCommands()) {
				String command = pendingCommand.command();

				if (pendingCommand.isOnlineRequired() && player == null) {
					this.plugin.logger().fine("Skipped command, player needs to be online: " + command);
					continue;
				}

				String uuid;
				String username;

				if (player != null) {
					uuid = NamelessAPI.javaUuidToWebsiteUuid(player.uuid());
					username = player.username();
				} else {
					uuid = customer.identifier();
					username = customer.username();
				}

				if (command.contains("{uuid}")) {
					if (uuid == null) {
						this.plugin.logger().warning("Skipped command, contains {uuid} placeholder while UUID is unknown.");
						continue;
					}
					command = command.replace("{uuid}", uuid);
				}

				command = command.replace("{username}", username);

				this.plugin.logger().info("Running command: " + command);
				this.plugin.audiences().console().dispatchCommand(command);
				completedCommands.add(pendingCommand);
			}
		}

		if (completedCommands.isEmpty()) {
			this.plugin.logger().fine("No completed commands");
			return;
		}

		this.plugin.logger().fine(() -> "Sending " + completedCommands.size() + " completed commands to website");

		this.plugin.scheduler().runAsync(() -> submitCompletedCommands(api, completedCommands));
	}

	public void submitCompletedCommands(NamelessAPI api, Collection<PendingCommandsResponse.PendingCommand> complete) {
		try {
			api.store().markCommandsExecuted(complete);
		} catch (NamelessException e) {
			this.plugin.logger().warning("Failed to mark commands as executed!");
			this.plugin.logger().warning("To prevent commands running in a loop, the store system will shut down (until the next reload).");
			this.plugin.logger().logException(e);
			if (this.commandTask != null) {
				this.commandTask.cancel();
			}
		}
	}

}
