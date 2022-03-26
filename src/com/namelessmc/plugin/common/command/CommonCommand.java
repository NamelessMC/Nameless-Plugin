package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class CommonCommand {

	private final @NotNull CommonObjectsProvider provider;
	private final @NotNull String configName;
	private final @NotNull LanguageHandler.Term usageTerm;
	private final @NotNull Permission permission;
	private final @Nullable String actualName;

	public CommonCommand(final @NotNull CommonObjectsProvider provider,
						 final @NotNull String configName,
						 final @NotNull LanguageHandler.Term usageTerm,
						 final @NotNull Permission permission) {
		this.provider = provider;
		this.configName = configName;
		this.usageTerm = usageTerm;
		this.permission = permission;
		this.actualName = this.provider.getCommandsConfig().isString(this.getConfigName())
				? this.provider.getCommandsConfig().getString(this.getConfigName())
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

	public @NotNull Permission getPermission() {
		return this.permission;
	}

	protected @NotNull AbstractScheduler getScheduler() {
		return this.provider.getScheduler();
	}

	protected @NotNull LanguageHandler getLanguage() {
		return this.provider.getLanguage();
	}

	protected @NotNull ApiProvider getApiProvider(){
		return this.provider.getApiProvider();
	}

	protected @NotNull Optional<NamelessAPI> getApi(){
		return this.getApiProvider().getNamelessApi();
	}

	protected @NotNull ExceptionLogger getExceptionLogger() { return this.provider.getExceptionLogger(); }

	public abstract void execute(CommandSender sender, String[] args);

	public static List<CommonCommand> getCommands(CommonObjectsProvider provider) {
		List<CommonCommand> list = new ArrayList<>();
		list.add(new GetNotificationsCommand(provider));
		list.add(new RegisterCommand(provider));
		list.add(new ReportCommand(provider));
		list.add(new UserInfoCommand(provider));
		list.add(new VerifyCommand(provider));
		return Collections.unmodifiableList(list);
	}

}
