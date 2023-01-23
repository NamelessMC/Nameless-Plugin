package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.command.AbstractScheduledTask;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GroupSync implements Reloadable {

    /**
     * Current group set for players, updated for online players
     */
    private final Map<UUID, Set<String>> playerGroups = new HashMap<>();

    private final NamelessPlugin plugin;

    private @Nullable AbstractScheduledTask task = null;

    GroupSync(final NamelessPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unload() {
        if (this.task != null) {
            task.cancel();
        }
    }

    @Override
    public void load() {
        this.plugin.scheduler().runAsync(() -> {
            try {
                final NamelessAPI api = this.plugin.apiProvider().api();
                if (api == null) {
                    return;
                }

                // Group sync API is available in 2.1.0+
                if (api.website().parsedVersion().minor() >= 1) {
                    this.plugin.logger().fine("New group sync API enabled!");
                    this.task = this.plugin.scheduler().runTimer(this::syncGroups, Duration.ofSeconds(10));
                } else {
                    this.plugin.logger().fine("New group sync not enabled, website must be v2.1.0+");
                }
            } catch (final NamelessException e) {
                this.plugin.logger().logException(e);
            }
        });
    }

    private void syncGroups() {
        final AbstractPermissions permissions = this.plugin.permissions();

        if (permissions == null) {
            this.plugin.logger().warning("Cannot check player groups for group sync, no permissions adapter is active.");
            return;
        }

        final Map<UUID, Set<String>> groupsToSend = new HashMap<>();

        for (NamelessPlayer player : this.plugin.audiences().onlinePlayers()) {
            final Set<String> newGroups = permissions.getPlayerGroups(player);
            if (newGroups == null) {
                this.plugin.logger().fine(() -> "Cannot retrieve groups for player: " + player.username());
                continue;
            }

            final Set<String> previousGroups = playerGroups.get(player.uuid());

            if (previousGroups == null) {
                this.plugin.logger().fine(() -> "Groups not previously known, or manually re-queued for player: " + player.username());
                this.playerGroups.put(player.uuid(), newGroups);
                groupsToSend.put(player.uuid(), newGroups);
            } else if (!newGroups.equals(previousGroups)) {
                this.plugin.logger().fine(() -> "Groups have changed for player: " + player.username());
                this.playerGroups.put(player.uuid(), newGroups);
                groupsToSend.put(player.uuid(), newGroups);
            }
        }

        if (groupsToSend.isEmpty()) {
            this.plugin.logger().fine("No group changes");
            return;
        }

        this.plugin.logger().fine(() -> "Sending groups for " + groupsToSend.size() + " players");

        final int serverId = this.plugin.config().main().node("server-data-sender", "server-id").getInt();

        if (serverId <= 0) {
            this.plugin.logger().warning("server-id is not configured");
            return;
        }

        this.plugin.scheduler().runAsync(() -> {
            final NamelessAPI api = this.plugin.apiProvider().api();
            if (api == null) {
                return;
            }
            try {
                api.sendMinecraftGroups(serverId, groupsToSend);
            } catch (NamelessException e) {
                this.plugin.logger().warning("An error occurred while sending player groups to the website, for group sync. The plugin will try again later.");
                this.plugin.logger().logException(e);

                // Re-queue players by deleting their groups from local state
                for (final UUID uuid : groupsToSend.keySet()) {
                    this.playerGroups.remove(uuid);
                }
            }
        });
    }

}
