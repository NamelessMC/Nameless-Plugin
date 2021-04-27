package com.namelessmc.plugin.spigot.commands;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AlreadyHasOpenReportException;
import com.namelessmc.java_api.exception.CannotReportSelfException;
import com.namelessmc.java_api.exception.ReportUserBannedException;
import com.namelessmc.java_api.exception.UnableToCreateReportException;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

public class ReportCommand extends Command {

	public ReportCommand() {
		super("report",
				Term.COMMAND_REPORT_DESCRIPTION,
				Term.COMMAND_REPORT_USAGE,
				Permission.COMMAND_REPORT);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length < 2) {
			return false;
		}

		final LanguageHandler<CommandSender> lang = NamelessPlugin.getInstance().getLanguageHandler();

		if (!(sender instanceof Player)) {
			lang.send(Term.COMMAND_NOTAPLAYER, sender);
			return false;
		}

		final Player player = (Player) sender;

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final String targetUsername = args[0];
				final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				final Optional<NamelessUser> user = NamelessPlugin.getInstance().getNamelessApi().getUser(player.getUniqueId());
				if (!user.isPresent()) {
					lang.send(Term.PLAYER_SELF_NOTREGISTERED, player);
					return;
				}

				final Optional<NamelessUser> target = NamelessPlugin.getInstance().getNamelessApi().getUser(targetUsername);
				if (!target.isPresent()) {
					lang.send(Term.PLAYER_OTHER_NOTREGISTERED, player);
					return;
				}

				user.get().createReport(target.get(), reason);
				lang.send(Term.COMMAND_REPORT_OUTPUT_SUCCESS, player);
			} catch (final ReportUserBannedException e) {
				lang.send(Term.PLAYER_SELF_COMMAND_BANNED, player);
			} catch (final AlreadyHasOpenReportException e) {
				lang.send(Term.COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN, player);
			} catch (final UnableToCreateReportException e) {
				lang.send(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC, player);
			} catch (final CannotReportSelfException e) {
				lang.send(Term.COMMAND_REPORT_OUTPUT_FAIL_REPORT_SELF, player);
			} catch (final NamelessException e) {
				lang.send(Term.COMMAND_REPORT_OUTPUT_FAIL_GENERIC, player);
				e.printStackTrace();
			}
		});
		return true;
	}

}