package com.namelessmc.plugin.common.command;

import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class NamelessPluginCommand extends CommonCommand {

	public NamelessPluginCommand(final @NonNull NamelessPlugin plugin) {
		super(
				plugin,
				"plugin",
				COMMAND_PLUGIN_USAGE,
				COMMAND_PLUGIN_DESCRIPTION,
				Permission.COMMAND_PLUGIN
		);

		if (this.actualName() == null) {
			plugin.logger().warning("The commands config file is missing the plugin command");
		}
	}

	@Override
	public void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		if (args.length == 1) {
			switch(args[0]) {
				case "reload":
				case "rl":
					this.plugin().reload();
					sender.sendMessage(this.language().get(COMMAND_PLUGIN_OUTPUT_RELOAD_SUCCESSFUL));
					return;
				case "last_api_error":
					final @Nullable Throwable t = this.plugin().apiProvider().getLastException();
					if (t != null) {
						t.printStackTrace();
						if (sender instanceof NamelessPlayer) {
							sender.sendMessage(Component.text("Last error has been printed to the console"));
						}
					} else {
						sender.sendMessage(Component.text("No error"));
					}
					return;
			}
		}

		sender.sendMessage(this.usage());
	}

	@Override
	public List<String> complete(@NonNull NamelessCommandSender sender, @NonNull String @NonNull [] args) {
		if (args.length == 1) {
			return Arrays.stream(new String[]{"reload", "rl", "last_api_error"}).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
