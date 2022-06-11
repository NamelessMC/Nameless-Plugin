package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class ReportCommand extends CommonCommand {

	public ReportCommand(final @NonNull NamelessPlugin plugin) {
		super(plugin,
				"report",
				COMMAND_REPORT_USAGE,
				COMMAND_REPORT_DESCRIPTION,
				Permission.COMMAND_REPORT);
	}

	@Override
	public void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		if (args.length < 2) {
			sender.sendMessage(this.usage());
			return;
		}

		if (sender instanceof NamelessConsole) {
			sender.sendMessage(language().get(COMMAND_NOT_A_PLAYER));
			return;
		}

		scheduler().runAsync(() -> {
			final NamelessAPI api = this.apiProvider().api();
			if (api == null) {
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				return;
			}

			try {
				final String targetUsername = args[0];
				final NamelessPlayer target = this.plugin().audiences().playerByUsername(targetUsername);
				if (target == null) {
					sender.sendMessage(language().get(ERROR_USERNAME_NOT_ONLINE));
					return;
				}

				final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				final NamelessUser user = api.userByMinecraftUuid(((NamelessPlayer) sender).uuid());
				if (user == null) {
					sender.sendMessage(language().get(PLAYER_SELF_NOT_REGISTERED));
					return;
				}
				user.createReport(target.uuid(), target.username(), reason);
				sender.sendMessage(language().get(COMMAND_REPORT_OUTPUT_SUCCESS));
			} catch (final NamelessException e) {
				if (e instanceof ApiException) {
					switch (((ApiException) e).apiError()) {
						case CORE_BANNED_FROM_WEBSITE:
							sender.sendMessage(language().get(PLAYER_SELF_COMMAND_BANNED));
							return;
						case CORE_OPEN_REPORT_ALREADY:
							sender.sendMessage(language().get(COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN));
							return;
						case CORE_CANNOT_REPORT_YOURSELF:
							sender.sendMessage(language().get(COMMAND_REPORT_OUTPUT_FAIL_REPORT_SELF));
							return;
					}
				}

				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				logger().logException(e);
			}
		});
	}

	@Override
	public List<String> complete(@NonNull NamelessCommandSender sender, @NonNull String @NonNull [] args) {
		if (args.length == 1) {
			return this.plugin().audiences().onlinePlayers().stream()
					.map(NamelessPlayer::username)
					.filter(s -> s.startsWith(args[0]))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
