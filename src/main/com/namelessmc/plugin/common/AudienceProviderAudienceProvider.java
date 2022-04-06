package com.namelessmc.plugin.common;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.AudienceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This is an "AbstractAudienceProvider" implementation using an "AudienceProvider" instance from adventure
 */
public class AudienceProviderAudienceProvider extends AbstractAudienceProvider {

	private final @NotNull AudienceProvider audienceProvider;

	public AudienceProviderAudienceProvider(final @NotNull AudienceProvider audienceProvider) {
		this.audienceProvider = audienceProvider;
	}

	@Override
	public Audience console() {
		return this.audienceProvider.console();
	}

	@Override
	public Audience all() {
		return this.audienceProvider.all();
	}

	@Override
	public Audience player(UUID uuid) {
		return this.audienceProvider.player(uuid);
	}

}
