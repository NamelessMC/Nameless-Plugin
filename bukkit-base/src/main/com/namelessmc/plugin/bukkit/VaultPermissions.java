package com.namelessmc.plugin.bukkit;

import com.namelessmc.plugin.bukkit.audiences.BukkitNamelessPlayer;
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
			log.warning("Vault is installed, but no vault-compatible permissions system is loaded. Is your permissions plugin compatible with Vault?");
			return;
		}
		final Permission permission = permissionProvider.getProvider();
		if (permission == null) {
			log.warning("Vault is installed, but no vault-compatible permissions system is loaded. Is your permissions plugin compatible with Vault?");
			return;
		}

		if (!permission.hasGroupSupport()) {
			log.warning("Vault is installed, but the loaded permissions system ('" + permission.getName() + "') does not support groups. Is your permissions plugin compatible with Vault?");
			return;
		}

		log.fine("Vault permissions seem to work");

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
		final Player bukkitPlayer = ((BukkitNamelessPlayer) player).bukkitPlayer();
		return Arrays.stream(this.permission.getPlayerGroups(bukkitPlayer)).collect(Collectors.toUnmodifiableSet());
	}

}
