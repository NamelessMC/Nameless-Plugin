package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AlreadyHasOpenReportException;
import com.namelessmc.java_api.exception.CannotReportSelfException;
import com.namelessmc.java_api.exception.ReportUserBannedException;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public class ReportCommand extends CommonCommand {

	public ReportCommand(final @NotNull NamelessPlugin plugin) {
		super(plugin,
				"report",
				Term.COMMAND_REPORT_USAGE,
				Term.COMMAND_REPORT_DESCRIPTION,
				Permission.COMMAND_REPORT);
	}

	@Override
	public void execute(final @NotNull NamelessCommandSender sender, final @NotNull String@NotNull[] args) {
		if (args.length < 2) {
			sender.sendMessage(this.getUsage());
			return;
		}

		if (sender instanceof NamelessConsole) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOT_A_PLAYER));
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (optApi.isEmpty()) {
				sender.sendMessage(this.getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final String targetUsername = args[0];
				final NamelessPlayer target = this.getPlugin().audiences().playerByUsername(targetUsername);
				if (target == null) {
					sender.sendMessage(getLanguage().getComponent(Term.ERROR_USERNAME_NOT_ONLINE));
					return;
				}

				final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				final Optional<NamelessUser> optUser = api.getUser(((NamelessPlayer) sender).getUniqueId());
				if (optUser.isEmpty()) {
					sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_NOT_REGISTERED));
					return;
				}

				final NamelessUser user = optUser.get();
				user.createReport(target.getUniqueId(), target.getUsername(), reason);
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_SUCCESS));
			} catch (final ReportUserBannedException e) {
				sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_COMMAND_BANNED));
			} catch (final AlreadyHasOpenReportException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN));
			} catch (final CannotReportSelfException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_FAIL_REPORT_SELF));
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				getLogger().logException(e);
			}
		});
	}

}
