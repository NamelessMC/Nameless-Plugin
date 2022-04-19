package com.namelessmc.plugin.common;

import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public abstract class AbstractAudienceProvider {

	public abstract @NotNull NamelessConsole console();

	public abstract @NotNull Audience broadcast();

	public abstract @Nullable NamelessPlayer player(final @NotNull UUID uuid);

	public abstract @Nullable NamelessPlayer playerByUsername(final @NotNull String username);

	public abstract @NotNull Collection<@NotNull NamelessPlayer> onlinePlayers();

}
