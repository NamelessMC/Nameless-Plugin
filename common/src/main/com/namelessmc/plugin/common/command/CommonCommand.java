package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.config.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class CommonCommand {

	private final @NotNull NamelessPlugin plugin;
	private final @NotNull String configName;
	private final @NotNull LanguageHandler.Term usageTerm;
	private final @NotNull LanguageHandler.Term descriptionTerm;
	private final @NotNull Permission permission;
	private final @Nullable String actualName;

	public CommonCommand(final @NotNull NamelessPlugin plugin,
						 final @NotNull String configName,
						 final @NotNull LanguageHandler.Term usageTerm,
						 final @NotNull LanguageHandler.Term descriptionTerm,
						 final @NotNull Permission permission) {
		this.plugin = plugin;
		this.configName = configName;
		this.usageTerm = usageTerm;
		this.descriptionTerm = descriptionTerm;
		this.permission = permission;
		final Configuration config = plugin.config().commands();
		this.actualName = config.contains(this.configName())
				? config.getString(this.configName())
				: null;
	}

	public @NotNull String configName() {
		return this.configName;
	}

	public @Nullable String actualName() {
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

	public @NotNull Permission permission() {
		return this.permission;
	}

	protected @NotNull NamelessPlugin plugin() {
		return this.plugin;
	}

	protected @NotNull AbstractScheduler scheduler() {
		return this.plugin.scheduler();
	}

	protected @NotNull LanguageHandler language() {
		return this.plugin.language();
	}

	protected @NotNull ApiProvider apiProvider(){
		return this.plugin.apiProvider();
	}

	protected @NotNull Optional<NamelessAPI> api(){
		return this.apiProvider().api();
	}

	protected @NotNull AbstractLogger logger() { return this.plugin.logger(); }

	public abstract void execute(final @NotNull NamelessCommandSender sender, final @NotNull String@NotNull[] args);

	public static List<CommonCommand> enabledCommands(final @NotNull NamelessPlugin plugin) {
		List<CommonCommand> list = new ArrayList<>();
		list.add(new GetNotificationsCommand(plugin));
		list.add(new NamelessPluginCommand(plugin));
		list.add(new RegisterCommand(plugin));
		list.add(new ReportCommand(plugin));
		list.add(new UserInfoCommand(plugin));
		list.add(new VerifyCommand(plugin));
		return list.stream()
				.filter(command -> command.actualName() != null)
				.collect(Collectors.toUnmodifiableList());
	}

}
