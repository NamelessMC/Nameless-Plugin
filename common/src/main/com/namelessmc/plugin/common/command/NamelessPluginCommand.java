package com.namelessmc.plugin.common.command;

import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessCommandSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NamelessPluginCommand extends CommonCommand {

	public NamelessPluginCommand(final @NonNull NamelessPlugin plugin) {
		super(
				plugin,
				"plugin",
				LanguageHandler.Term.COMMAND_PLUGIN_USAGE,
				LanguageHandler.Term.COMMAND_PLUGIN_DESCRIPTION,
				Permission.COMMAND_PLUGIN
		);
	}

	@Override
	public void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		if (args.length == 1 && (args[0].equals("reload") || args[0].equals("rl"))) {
			this.plugin().reload();
			sender.sendMessage(this.language().get(LanguageHandler.Term.COMMAND_PLUGIN_OUTPUT_RELOAD_SUCCESSFUL));
		} else {
			sender.sendMessage(this.usage());
		}
	}
}
