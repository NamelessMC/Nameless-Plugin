package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.*;
import com.namelessmc.java_api.integrations.DetailedIntegrationData;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserInfoCommand extends CommonCommand {

	public UserInfoCommand(final @NotNull NamelessPlugin plugin) {
		super(plugin,
				"user-info",
				Term.COMMAND_USERINFO_USAGE,
				Term.COMMAND_USERINFO_DESCRIPTION,
				Permission.COMMAND_USER_INFO);
	}

	@Override
	public void execute(final @NotNull NamelessCommandSender sender, final @NotNull String@NotNull[] args) {
		if (args.length == 0) {
			if (sender instanceof NamelessConsole) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOT_A_PLAYER));
				return;
			}

			// Player itself as first argument
			execute(sender, new String[] { ((NamelessPlayer) sender).getUsername() });
			return;
		}

		if (args.length != 1) {
			sender.sendMessage(this.getUsage());
			return;
		}

		getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (optApi.isEmpty()) {
				sender.sendMessage(getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				return;
			}
			final NamelessAPI api = optApi.get();

			final Optional<NamelessUser> targetOptional;

			try {
				targetOptional = api.getUser(args[0]);

				if (targetOptional.isEmpty()) {
					sender.sendMessage(getLanguage().getComponent(Term.ERROR_WEBSITE_USERNAME_NOT_EXIST));
					return;
				}

				final NamelessUser user = targetOptional.get();

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_USERNAME, "username", user.getUsername()));
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_DISPLAY_NAME, "displayname", user.getDisplayName()));

				user.getPrimaryGroup().ifPresent(group -> {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP,
							"groupname", group.getName(),
							"id", String.valueOf(group.getId())));
				});

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_ALL_GROUPS,
						"groups_names_list", user.getGroups().stream().map(Group::getName).collect(Collectors.joining(", "))));

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_REGISTER_DATE,
						"date", this.getPlugin().dateFormatter().format(user.getRegisteredDate())));

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_VALIDATED,
						Placeholder.component("validated", getLanguage().getBooleanText(user.isVerified(), true))));

				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_BANNED,
						Placeholder.component("banned", getLanguage().getBooleanText(user.isBanned(), false))));

				for (CustomProfileFieldValue customField : user.getProfileFields()) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_CUSTOM_FIELD,
							"name", customField.getField().getName(), "value", customField.getValue()));
				}

				Map<String, DetailedIntegrationData> integrations = user.getIntegrations();
				if (!integrations.isEmpty()) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_INTEGRATIONS_HEADER));
					integrations.forEach((name, data) -> {
						sender.sendMessage(Component.text("  " + name + ":"));
						final Component indent = Component.text("    ");
						sender.sendMessage(indent.append(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_INTEGRATIONS_IDENTIFIER,
								"identifier", data.getIdentifier())));
						sender.sendMessage(indent.append(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_INTEGRATIONS_USERNAME,
								"username", data.getUsername())));
						sender.sendMessage(indent.append(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_INTEGRATIONS_LINKED_DATE,
								"linked_date", this.getPlugin().dateFormatter().format(data.getLinkedDate()))));
						sender.sendMessage(indent.append(getLanguage().getComponent(Term.COMMAND_USERINFO_OUTPUT_INTEGRATIONS_VERIFIED,
								Placeholder.component("is_verified", getLanguage().getBooleanText(data.isVerified(), true)))));
					});
				}
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				getLogger().logException(e);
			}
		});
	}

}
