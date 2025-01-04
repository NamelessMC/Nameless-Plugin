package com.namelessmc.plugin.common.audiences;

import com.namelessmc.plugin.common.ConfigurationHandler;
import net.kyori.adventure.audience.Audience;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public abstract class NamelessPlayer extends NamelessCommandSender {

	private final UUID uuid;
	private final String username;

	public NamelessPlayer(final ConfigurationHandler config,
						  final Audience audience,
						  final UUID uuid,
						  final String username) {
		super(audience);
		if (config.main().node("api", "offline-uuids").getBoolean()) {
			this.uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
		} else {
			this.uuid = uuid;
		}
		this.username = username;
	}

	public UUID uuid() {
		return this.uuid;
	}

	public String username() {
		return this.username;
	}
	
	public boolean isVanished() {
		return false;
	}

}
