package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AlreadyHasOpenReportException;
import com.namelessmc.java_api.exception.CannotReportSelfException;
import com.namelessmc.java_api.exception.ReportUserBannedException;
import com.namelessmc.java_api.exception.UnableToCreateReportException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import xyz.derkades.derkutils.bukkit.UUIDFetcher;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class ReportCommand extends CommonCommand {

	public ReportCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args, final String usage) {
		if (args.length < 2) {
			sender.sendLegacyMessage(usage);
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(this.getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final String targetUsername = args[0];
				final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				final Optional<NamelessUser> optUser = super.useUuids() ? api.getUser(sender.getUniqueId()) : api.getUser(sender.getName());
				if (!optUser.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				NamelessUser user = optUser.get();

				if (this.useUuids()) {
					UUID targetUuid = UUIDFetcher.getUUID(targetUsername);
					if (targetUuid == null) {
						sender.sendMessage(getLanguage().getComponent(Term.PLAYER_OTHER_NOTFOUND));
					} else {
						user.createReport(targetUuid, targetUsername, reason);
						sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_SUCCESS));
					}
				} else {
					Optional<NamelessUser> optTargetUser = api.getUser(targetUsername);
					if (optTargetUser.isPresent()) {
						user.createReport(optTargetUser.get(), reason);
					} else {
						sender.sendMessage(getLanguage().getComponent(Term.PLAYER_OTHER_NOTREGISTERED));
					}
				}
			} catch (final ReportUserBannedException e) {
				sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_COMMAND_BANNED));
			} catch (final AlreadyHasOpenReportException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN));
			} catch (final UnableToCreateReportException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC));
			} catch (final CannotReportSelfException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_FAIL_REPORT_SELF));
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC));
				getExceptionLogger().logException(e);
			}
		});
	}

}
