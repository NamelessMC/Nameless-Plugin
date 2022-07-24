package com.namelessmc.plugin.common.command;

import com.namelessmc.plugin.common.ApiProvider;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Collections;
import java.util.List;

import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_NO_PERMISSION;

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
		final CommentedConfigurationNode config = plugin.config().commands();
		this.actualName = config.node(configName).getString();
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

	protected @NonNull ApiProvider apiProvider() {
		return this.plugin.apiProvider();
	}

	protected @NonNull AbstractLogger logger() { return this.plugin.logger(); }

	protected abstract void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args);

	public void verifyPermissionThenExecute(NamelessCommandSender sender, String[] args) {
		if (!sender.hasPermission(this.permission)) {
			this.language().get(COMMAND_NO_PERMISSION);
		}

		this.execute(sender, args);
	}

	public List<String> complete(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		return Collections.emptyList();
	}

	public static List<CommonCommand> commands(final @NonNull NamelessPlugin plugin) {
		return List.of(
				new GetNotificationsCommand(plugin),
				new NamelessPluginCommand(plugin),
				new RegisterCommand(plugin),
				new ReportCommand(plugin),
				new UserInfoCommand(plugin),
				new StoreChangeCreditsCommand(plugin),
				new VerifyCommand(plugin)
		);
	}

}
