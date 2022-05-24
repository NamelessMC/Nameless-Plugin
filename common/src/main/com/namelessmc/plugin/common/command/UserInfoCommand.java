package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.*;
import com.namelessmc.java_api.integrations.DetailedIntegrationData;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class UserInfoCommand extends CommonCommand {

	public UserInfoCommand(final @NonNull NamelessPlugin plugin) {
		super(plugin,
				"user-info",
				COMMAND_USERINFO_USAGE,
				COMMAND_USERINFO_DESCRIPTION,
				Permission.COMMAND_USER_INFO);
	}

	@Override
	public void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		if (args.length == 0 && sender instanceof NamelessPlayer) {
			// No username specified, try to find NamelessMC account for this Minecraft player
			this.scheduler().runAsync(() -> {
				try {
					final NamelessAPI api = this.apiProvider().api();
					if (api == null) {
						sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
						return;
					}

					Optional<NamelessUser> userOptional = api.getUserByMinecraftUuid(((NamelessPlayer) sender).uuid());
					if (userOptional.isPresent()) {
						this.scheduler().runSync(() -> printInfoForUser(sender, userOptional.get()));
					} else {
						sender.sendMessage(language().get(PLAYER_SELF_NOT_REGISTERED));
					}
				} catch (NamelessException e) {
					sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
					logger().logException(e);
				}
			});
			return;
		} else if (args.length == 1) {
			// Find NamelessMC user by provided username in command argument
			this.scheduler().runAsync(() -> {
				try {
					final NamelessAPI api = this.apiProvider().api();
					if (api == null) {
						sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
						return;
					}

					Optional<NamelessUser> userOptional;
					NamelessPlayer targetPlayer = this.plugin().audiences().playerByUsername(args[0]);
					if (targetPlayer != null) {
						userOptional = api.getUserByMinecraftUuid(targetPlayer.uuid());
						if (userOptional.isEmpty()) {
							sender.sendMessage(language().get(ERROR_TARGET_NO_WEBSITE_ACCOUNT));
							return;
						}
					} else if (args[0].matches(".+#\\d{4}")) {
						// Likely a discord username
						userOptional = api.getUserByDiscordUsername(args[0]);
						if (userOptional.isEmpty()) {
							sender.sendMessage(language().get(ERROR_DISCORD_USERNAME_NOT_EXIST));
							return;
						}
					} else {
						try {
							// Maybe a UUID?
							userOptional = api.getUserByMinecraftUuid(UUID.fromString(args[0]));
							if (userOptional.isEmpty()) {
								sender.sendMessage(language().get(ERROR_MINECRAFT_UUID_NOT_EXIST));
								return;
							}
						} catch (final IllegalArgumentException e) {
							// Lookup by username
							userOptional = api.getUserByUsername(args[0]);
							if (userOptional.isEmpty()) {
								sender.sendMessage(language().get(ERROR_WEBSITE_USERNAME_NOT_EXIST));
								return;
							}
						}
					}

					final NamelessUser user = userOptional.get();
					user.getUsername(); // Force user info to load now, asynchronously
					this.scheduler().runSync(() -> printInfoForUser(sender, user));
				} catch (NamelessException e) {
					sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
					logger().logException(e);
				}
			});
			return;
		}

		sender.sendMessage(this.usage());
	}

	private void printInfoForUser(final @NonNull NamelessCommandSender sender,
								  final @NonNull NamelessUser user) {
		try {
			sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_USERNAME, "username", user.getUsername()));
			sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_DISPLAY_NAME, "displayname", user.getDisplayName()));

			user.getPrimaryGroup().ifPresent(group ->
					sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP,
							"groupname", group.getName(),
							"id", String.valueOf(group.getId()))));

			sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_ALL_GROUPS,
					"groups_names_list", user.getGroups().stream().map(Group::getName).collect(Collectors.joining(", "))));

			sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_REGISTER_DATE,
					"date", this.plugin().dateFormatter().format(user.getRegisteredDate())));

			sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_VALIDATED,
					Placeholder.component("validated", language().booleanText(user.isVerified(), true))));

			sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_BANNED,
					Placeholder.component("banned", language().booleanText(user.isBanned(), false))));

			for (final CustomProfileFieldValue customField : user.getProfileFields()) {
				String value = customField.getValue();
				if (value == null) {
					value = "-";
				}
				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_CUSTOM_FIELD,
						"name", customField.getField().getName(), "value", value));
			}

			Map<String, DetailedIntegrationData> integrations = user.getIntegrations();
			if (!integrations.isEmpty()) {
				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_HEADER));
				integrations.forEach((name, data) -> {
					sender.sendMessage(Component.text("  " + name + ":"));
					final Component indent = Component.text("    ");
					sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_IDENTIFIER,
							"identifier", data.getIdentifier())));
					sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_USERNAME,
							"username", data.getUsername())));
					sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_LINKED_DATE,
							"linked_date", this.plugin().dateFormatter().format(data.getLinkedDate()))));
					sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_VERIFIED,
							Placeholder.component("is_verified", language().booleanText(data.isVerified(), true)))));
				});
			}
		} catch (NamelessException e) {
			sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
			logger().logException(e);
		}
	}

	@Override
	public List<String> complete(@NonNull NamelessCommandSender sender, @NonNull String @NonNull [] args) {
		if (args.length == 1) {
			return this.plugin().userCache().getUsernames().stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
