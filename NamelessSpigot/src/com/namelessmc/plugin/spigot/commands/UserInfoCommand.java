package com.namelessmc.plugin.spigot.commands;

import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.Group;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

public class UserInfoCommand extends Command {

	public UserInfoCommand() {
		super("user-info",
				Term.COMMAND_USERINFO_DESCRIPTION,
				Term.COMMAND_USERINFO_USAGE,
				Permission.COMMAND_USER_INFO);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		final LanguageHandler<CommandSender> lang = NamelessPlugin.getInstance().getLanguageHandler();

		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				lang.send(Term.COMMAND_NOTAPLAYER, sender);
				return true;
			}

			// Player itself as first argument
			return execute(sender, new String[] { sender.getName() });
		}

		if (args.length != 1) {
			return false;
		}

		Bukkit.getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final Optional<NamelessUser> targetOptional;

			try {
				targetOptional = NamelessPlugin.getInstance().getNamelessApi().getUser(args[0]);

				if (!targetOptional.isPresent()) {
					lang.send(Term.PLAYER_OTHER_NOTREGISTERED, sender);
					return;
				}

				final NamelessUser user = targetOptional.get();

				final String yes = lang.getMessage(Term.COMMAND_USERINFO_OUTPUT_YES);
				final String no = lang.getMessage(Term.COMMAND_USERINFO_OUTPUT_NO);

				final String validated = user.isVerified() ? yes : no;
				final String banned = user.isBanned() ? yes : no;

				lang.send(Term.COMMAND_USERINFO_OUTPUT_USERNAME, sender, "username", user.getUsername());
				lang.send(Term.COMMAND_USERINFO_OUTPUT_DISPLAYNAME, sender, "displayname", user.getDisplayName());
				lang.send(Term.COMMAND_USERINFO_OUTPUT_UUID, sender, "uuid", user.getUniqueId().isPresent() ? user.getUniqueId().get().toString() : lang.getMessage(Term.COMMAND_USERINFO_OUTPUT_UUID_UNKNOWN));
				lang.send(Term.COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP, sender, "groupname", user.getPrimaryGroup().get().getName(), "id", user.getPrimaryGroup().get().getId());
				lang.send(Term.COMMAND_USERINFO_OUTPUT_ALL_GROUPS, sender, "groups_names_list", user.getGroups().stream().map(Group::getName).collect(Collectors.joining(", ")));
				lang.send(Term.COMMAND_USERINFO_OUTPUT_REGISTERDATE, sender, "date", user.getRegisteredDate()); // TODO Format nicely (add option in config for date format)
				lang.send(Term.COMMAND_USERINFO_OUTPUT_VALIDATED, sender, "validated", validated);
				lang.send(Term.COMMAND_USERINFO_OUTPUT_BANNED, sender, "banned", banned);
			} catch (final NamelessException e) {
				sender.sendMessage(e.getMessage());
				e.printStackTrace();
				return;
			}
		});
		return true;
	}

}