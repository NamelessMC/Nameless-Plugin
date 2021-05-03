package com.namelessmc.plugin.common.command;

import java.util.Optional;
import java.util.stream.Collectors;

import com.namelessmc.java_api.Group;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;

public class UserInfoCommand extends CommonCommand {

	public UserInfoCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if (args.length == 0) {
			if (!sender.isPlayer()) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTAPLAYER));
				return;
			}

			// Player itself as first argument
			execute(sender, new String[] { sender.getName() });
		}

		if (args.length != 1) {
			// TODO send help text
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessUser> targetOptional;

			try {
				targetOptional = NamelessPlugin.getInstance().getNamelessApi().getUser(args[0]);

				if (!targetOptional.isPresent()) {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTAPLAYER));
					return;
				}

				final NamelessUser user = targetOptional.get();

				final String yes = getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_YES);
				final String no = getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_NO);

				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_USERNAME), "username", user.getUsername());
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_DISPLAYNAME), "displayname", user.getDisplayName());

				final String uuid = user.getUniqueId().isPresent() ? user.getUniqueId().get().toString() : getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_UUID_UNKNOWN);
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_UUID), "{uuid}", uuid);

				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP),
						"{groupname}", user.getPrimaryGroup().get().getName(),
						"{id}", String.valueOf(user.getPrimaryGroup().get().getId()));

				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_ALL_GROUPS),
						"{groups_names_list}", user.getGroups().stream().map(Group::getName).collect(Collectors.joining(", ")));

				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_REGISTERDATE),
						"{date}", user.getRegisteredDate().toString()); // TODO Format nicely (add option in config for date format)

				final String validated = user.isVerified() ? yes : no;
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_VALIDATED),
						"{validated}", validated);

				final String banned = user.isBanned() ? yes : no;
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_USERINFO_OUTPUT_BANNED),
						"{banned}", banned);
			} catch (final NamelessException e) {
				sender.sendMessage(e.getMessage());
				e.printStackTrace();
				return;
			}
		});
	}

}
