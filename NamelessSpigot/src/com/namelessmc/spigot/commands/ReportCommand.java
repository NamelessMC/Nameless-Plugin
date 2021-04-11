package com.namelessmc.spigot.commands;

import java.util.Arrays;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AlreadyHasOpenReportException;
import com.namelessmc.java_api.exception.ReportUserBannedException;
import com.namelessmc.java_api.exception.UnableToCreateReportException;
import com.namelessmc.spigot.Config;
import com.namelessmc.spigot.Message;
import com.namelessmc.spigot.NamelessPlugin;
import com.namelessmc.spigot.Permission;

public class ReportCommand extends Command {

	public ReportCommand() {
		super(Config.COMMANDS.getConfig().getString("report"),
				Message.COMMAND_REPORT_DESCRIPTION.getMessage(),
				Message.COMMAND_REPORT_USAGE.getMessage("command", Config.COMMANDS.getConfig().getString("report")),
				Permission.COMMAND_REPORT);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length < 2) {
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(Message.COMMAND_NOTAPLAYER.getMessage());
			return false;
		}

		final Player player = (Player) sender;

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final String targetUsername = args[0];
				final String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
				final Optional<NamelessUser> user = NamelessPlugin.getApi().getUser(player.getUniqueId());
				if (!user.isPresent()) {
					sender.sendMessage(Message.PLAYER_SELF_NOTREGISTERED.getMessage());
					return;
				}

				final Optional<NamelessUser> target = NamelessPlugin.getApi().getUser(targetUsername);
				if (!target.isPresent()) {
					sender.sendMessage(Message.PLAYER_OTHER_NOTREGISTERED.getMessage());
					return;
				}

				user.get().createReport(target.get(), reason);
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_SUCCESS.getMessage());
			} catch (final ReportUserBannedException e) {
				sender.sendMessage(Message.PLAYER_SELF_COMMAND_BANNED.getMessage());
			} catch (final AlreadyHasOpenReportException e) {
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN.getMessage());
			} catch (final UnableToCreateReportException e) {
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_FAIL_GENERIC.getMessage());
			} catch (final NamelessException e) {
				sender.sendMessage(Message.COMMAND_REPORT_OUTPUT_FAIL_GENERIC.getMessage());
				e.printStackTrace();
				return;
			}
		});
		return true;
	}

}