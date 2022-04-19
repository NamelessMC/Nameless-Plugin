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
		final Configuration config = plugin.config().getCommandsConfig();
		this.actualName = config.contains(this.getConfigName())
				? config.getString(this.getConfigName())
				: null;
	}

	public @NotNull String getConfigName() {
		return this.configName;
	}

	public @Nullable String getActualName() {
		return this.actualName;
	}

	public Component getUsage() {
		if (this.actualName == null) {
			throw new IllegalStateException("Cannot get usage for disabled command");
		}
		return this.getLanguage().getComponent(this.usageTerm, "command", this.actualName);
	}

	public Component getDescription() {
		return this.getLanguage().getComponent(this.descriptionTerm);
	}

	public @NotNull Permission getPermission() {
		return this.permission;
	}

	protected @NotNull NamelessPlugin getPlugin() {
		return this.plugin;
	}

	protected @NotNull AbstractScheduler getScheduler() {
		return this.plugin.scheduler();
	}

	protected @NotNull LanguageHandler getLanguage() {
		return this.plugin.language();
	}

	protected @NotNull ApiProvider getApiProvider(){
		return this.plugin.api();
	}

	protected @NotNull Optional<NamelessAPI> getApi(){
		return this.getApiProvider().getNamelessApi();
	}

	protected @NotNull AbstractLogger getLogger() { return this.plugin.logger(); }

	public abstract void execute(final @NotNull NamelessCommandSender sender, final @NotNull String@NotNull[] args);

	public static List<CommonCommand> getEnabledCommands(final @NotNull NamelessPlugin plugin) {
		List<CommonCommand> list = new ArrayList<>();
		list.add(new GetNotificationsCommand(plugin));
		list.add(new NamelessPluginCommand(plugin));
		list.add(new RegisterCommand(plugin));
		list.add(new ReportCommand(plugin));
		list.add(new UserInfoCommand(plugin));
		list.add(new VerifyCommand(plugin));
		return list.stream()
				.filter(command -> command.getActualName() != null)
				.collect(Collectors.toUnmodifiableList());
	}

}
