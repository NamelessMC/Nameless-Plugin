package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.common.AbstractPermissions;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class VaultPermissions extends AbstractPermissions {

	private final NamelessPlugin plugin;

	private @Nullable Permission permission;

	public VaultPermissions(final NamelessPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void unload() {
		this.permission = null;
	}

	@Override
	public void load() {
		final AbstractLogger log = this.plugin.logger();
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
			log.fine("Vault is not installed.");
			return;
		}
		final RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider == null) {
			log.fine("No vault compatible permissions plugin was found.");
			return;
		}
		final Permission permission = permissionProvider.getProvider();
		if (permission == null) {
			log.fine("No vault compatible permissions plugin was found.");
			return;
		}

		try {
			permission.getGroups();
		} catch (final UnsupportedOperationException ignored) {
			log.fine("Vault permissions plugin doesn't seem to work.");
			return;
		}

		log.fine("Vault permissions plugin found, seems to work");

		this.permission = permission;
	}

	@Override
	public boolean isUsable() {
		return this.permission != null;
	}

	@Override
	public Set<String> getGroups() {
		if (this.permission == null) {
			throw new ProviderNotUsableException();
		}
		return Arrays.stream(this.permission.getGroups()).collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Set<String> getPlayerGroups(final @NonNull NamelessPlayer player) {
		if (this.permission == null) {
			throw new ProviderNotUsableException();
		}
		final Player bukkitPlayer = Bukkit.getPlayer(player.uuid());
		if (bukkitPlayer == null) {
			return null;
		}
		return Arrays.stream(this.permission.getPlayerGroups(bukkitPlayer)).collect(Collectors.toUnmodifiableSet());
	}

}
