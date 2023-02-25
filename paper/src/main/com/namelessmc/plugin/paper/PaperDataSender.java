package com.namelessmc.plugin.paper;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.JsonObject;
import com.namelessmc.plugin.bukkit.BukkitDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.URL;

public class PaperDataSender extends BukkitDataSender {

	protected PaperDataSender(@NonNull NamelessPlugin plugin, @NonNull PaperNamelessPlugin bukkitPlugin) {
		super(plugin, bukkitPlugin);
	}

	@Override
	protected void registerCustomProviders() {
		super.registerCustomProviders();

		this.registerPlayerInfoProvider((json, namelessPlayer) -> {
			Player player = Bukkit.getPlayer(namelessPlayer.uuid());
			if (player == null) {
				throw new IllegalStateException();
			}

			PlayerProfile profile = player.getPlayerProfile();
			PlayerTextures textures = profile.getTextures();
			JsonObject skinJson = new JsonObject();
			skinJson.addProperty("model", textures.getSkinModel().toString().toLowerCase());
			URL skin = textures.getSkin();
			URL cape = textures.getCape();
			if (skin != null) {
				skinJson.addProperty("skin", skin.toString());
			}
			if (cape != null) {
				skinJson.addProperty("cape", cape.toString());
			}
			json.add("skin", skinJson);
		});

		this.registerGlobalInfoProvider(json -> {
			final Component motd = Bukkit.getServer().motd();
			final String legacyMotd = LegacyComponentSerializer.legacySection().serialize(motd);
			json.addProperty("motd", legacyMotd);
		});
	}

}
