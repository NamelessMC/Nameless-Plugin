package com.namelessmc.plugin.common;

import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.UUID;

public abstract class AbstractAudienceProvider {

	public abstract @NonNull NamelessConsole console();

	public abstract @NonNull Audience broadcast();

	public abstract @Nullable NamelessPlayer player(final @NonNull UUID uuid);

	public abstract @Nullable NamelessPlayer playerByUsername(final @NonNull String username);

	public abstract @NonNull Collection<@NonNull NamelessPlayer> onlinePlayers();

}
