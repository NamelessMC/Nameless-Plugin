package com.namelessmc.plugin.common.command;

import java.util.Arrays;
import java.util.Optional;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AlreadyHasOpenReportException;
import com.namelessmc.java_api.exception.CannotReportSelfException;
import com.namelessmc.java_api.exception.ReportUserBannedException;
import com.namelessmc.java_api.exception.UnableToCreateReportException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;

public class ReportCommand extends CommonCommand {

	public ReportCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args, final String usage) {
		if (args.length < 2) {
			sender.sendMessage(usage);
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTAPLAYER));
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(this.getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final String targetUsername = args[0];
				final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				final Optional<NamelessUser> user = api.getUser(sender.getUniqueId());
				if (!user.isPresent()) {
					sender.sendMessage(getLanguage().getMessage(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				final Optional<NamelessUser> target = api.getUser(targetUsername);
				if (!target.isPresent()) {
					sender.sendMessage(getLanguage().getMessage(Term.PLAYER_OTHER_NOTREGISTERED));
					return;
				}

				user.get().createReport(target.get(), reason);
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_SUCCESS));
			} catch (final ReportUserBannedException e) {
				sender.sendMessage(getLanguage().getMessage(Term.PLAYER_SELF_COMMAND_BANNED));
			} catch (final AlreadyHasOpenReportException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN));
			} catch (final UnableToCreateReportException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC));
			} catch (final CannotReportSelfException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_FAIL_REPORT_SELF));
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC));
				e.printStackTrace();
			}
		});
	}

}
