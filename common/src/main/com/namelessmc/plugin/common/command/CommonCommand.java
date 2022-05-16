package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.config.Configuration;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class CommonCommand {

	private final @NonNull NamelessPlugin plugin;
	private final LanguageHandler.@NonNull Term usageTerm;
	private final LanguageHandler.@NonNull Term descriptionTerm;
	private final @NonNull Permission permission;
	private final @Nullable String actualName;

	public CommonCommand(final @NonNull NamelessPlugin plugin,
						 final @NonNull String configName,
						 final LanguageHandler.@NonNull Term usageTerm,
						 final LanguageHandler.@NonNull Term descriptionTerm,
						 final @NonNull Permission permission) {
		this.plugin = plugin;
		this.usageTerm = usageTerm;
		this.descriptionTerm = descriptionTerm;
		this.permission = permission;
		final Configuration config = plugin.config().commands();
		this.actualName = config.contains(configName)
				? config.getString(configName)
				: null;
	}

	public @Nullable String actualName(@UnknownInitialization(CommonCommand.class) CommonCommand this) {
		return this.actualName;
	}

	public Component usage() {
		if (this.actualName == null) {
			throw new IllegalStateException("Cannot get usage for disabled command");
		}
		return this.language().get(this.usageTerm, "command", this.actualName);
	}

	public Component description() {
		return this.language().get(this.descriptionTerm);
	}

	public @NonNull Permission permission() {
		return this.permission;
	}

	protected @NonNull NamelessPlugin plugin() {
		return this.plugin;
	}

	protected @NonNull AbstractScheduler scheduler() {
		return this.plugin.scheduler();
	}

	protected @NonNull LanguageHandler language() {
		return this.plugin.language();
	}

	protected @NonNull ApiProvider apiProvider(){
		return this.plugin.apiProvider();
	}

	protected @NonNull Optional<NamelessAPI> api(){
		return this.apiProvider().api();
	}

	protected @NonNull AbstractLogger logger() { return this.plugin.logger(); }

	public abstract void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args);

	public List<String> complete(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		return Collections.emptyList();
	}

	public static List<CommonCommand> commands(final @NonNull NamelessPlugin plugin) {
		ArrayList<CommonCommand> list = new ArrayList<>();
		list.add(new GetNotificationsCommand(plugin));
		list.add(new NamelessPluginCommand(plugin));
		list.add(new RegisterCommand(plugin));
		list.add(new ReportCommand(plugin));
		list.add(new UserInfoCommand(plugin));
		list.add(new VerifyCommand(plugin));
		list.trimToSize();
		return list;
	}

}
