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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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
				final Optional<NamelessUser> user = super.useUuids() ? api.getUser(sender.getUniqueId()) : api.getUser(sender.getName());
				if (!user.isPresent()) {
					sender.sendMessage(getLanguage().getMessage(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				final Optional<NamelessUser> target = api.getUser(targetUsername);
				if (target.isPresent()) {
					user.get().createReport(target.get(), reason);
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_SUCCESS));
				} else {
					if (this.useUuids()) {
						// this is deprecated but really the best option since it uses the server's cache
						// if everyone used paper we could use their method which only returns if cached
						OfflinePlayer player = Bukkit.getOfflinePlayer(targetUsername);
						if (player.hasPlayedBefore()) {
							user.get().createReport(player.getUniqueId(), targetUsername, reason);
							sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REPORT_OUTPUT_SUCCESS));
						} else {
							sender.sendMessage(getLanguage().getMessage(Term.PLAYER_OTHER_NOTFOUND));
						}
					} else {
						sender.sendMessage(getLanguage().getMessage(Term.PLAYER_OTHER_NOTREGISTERED));
					}
				}
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
