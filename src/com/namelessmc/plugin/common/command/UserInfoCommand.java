package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.*;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserInfoCommand extends CommonCommand {

	public UserInfoCommand(final @NotNull CommonObjectsProvider provider) {
		super(provider,
				"user-info",
				Term.COMMAND_USERINFO_USAGE,
				Permission.COMMAND_USER_INFO);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if (args.length == 0) {
			if (!sender.isPlayer()) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
				return;
			}

			// Player itself as first argument
			execute(sender, new String[] { sender.getName() });
			return;
		}

		if (args.length != 1) {
			sender.sendMessage(this.getUsage());
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_FAIL));
				return;
			}
			final NamelessAPI api = optApi.get();

			final Optional<NamelessUser> targetOptional;

			try {
				targetOptional = api.getUser(args[0]);

				if (!targetOptional.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
					return;
				}

				final NamelessUser user = targetOptional.get();

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_USERNAME, "username", user.getUsername()));
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_DISPLAYNAME, "displayname", user.getDisplayName()));

				Optional<UUID> uuid = user.getUniqueId();
				if (uuid.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_UUID, "uuid", uuid.toString()));
				} else {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_UUID_UNKNOWN));
				}

				user.getPrimaryGroup().ifPresent(group -> {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP,
							"groupname", group.getName(),
							"id", String.valueOf(group.getId())));
				});

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_ALL_GROUPS,
						"groups_names_list", user.getGroups().stream().map(Group::getName).collect(Collectors.joining(", "))));

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_REGISTERDATE,
						"date", user.getRegisteredDate().toString())); // TODO Format nicely (add option in config for date format)

				// TODO support formatting for yes/no somehow
				final String yes = getLanguage().getRawMessage(Term.COMMAND_USERINFO_OUTPUT_YES);
				final String no = getLanguage().getRawMessage(Term.COMMAND_USERINFO_OUTPUT_NO);

				final String validated = user.isVerified() ? yes : no;
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_VALIDATED,
						"validated", validated));

				final String banned = user.isBanned() ? yes : no;
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_BANNED,
						"banned", banned));

				for (CustomProfileFieldValue customField : user.getProfileFields()) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_CUSTOM_FIELD,
							"name", customField.getField().getName(), "value", customField.getValue()));
				}
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_FAIL));
				getLogger().logException(e);
			}
		});
	}

}
