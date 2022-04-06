package com.namelessmc.plugin.common;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class AbstractAudienceProvider {

	public abstract Audience console();

	public abstract Audience all();

	public abstract Audience player(final @NotNull UUID uuid);

}
