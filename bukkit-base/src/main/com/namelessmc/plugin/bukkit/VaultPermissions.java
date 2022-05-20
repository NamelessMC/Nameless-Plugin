package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.AbstractPermissions;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class VaultPermissions extends AbstractPermissions {

	private final @NonNull Permission permission;

	private VaultPermissions(final @NonNull Permission permission) {
		this.permission = permission;
	}

	@Override
	public Collection<String> getGroups() {
		return Arrays.asList(this.permission.getGroups());
	}

	@Override
	public Collection<String> getPlayerGroups(final @NonNull NamelessPlayer player) {
		final Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
		if (bukkitPlayer == null) {
			throw new IllegalStateException("Player " + player.username() + " is offline");
		}
		return Arrays.asList(this.permission.getPlayerGroups(bukkitPlayer));
	}

	static @Nullable VaultPermissions create(final @NonNull NamelessPlugin plugin) {
		final AbstractLogger log = plugin.logger();
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
			log.warning("Vault is not installed. Group sync will not work.");
			return null;
		}
		final RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider == null) {
			log.warning("No vault compatible permissions plugin was found. Group sync will not work.");
			return null;
		}
		final Permission permission = permissionProvider.getProvider();
		if (permission == null) {
			log.warning("No vault compatible permissions plugin was found. Group sync will not work.");
			return null;
		}

		try {
			permission.getGroups();
		} catch (final UnsupportedOperationException ignored) {
			log.warning("Permission plugin doesn't seem to work. Group sync will not work.");
			return null;
		}

		return new VaultPermissions(permission);
	}

}
