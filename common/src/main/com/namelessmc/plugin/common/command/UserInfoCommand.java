package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.CustomProfileFieldValue;
import com.namelessmc.java_api.Group;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.ApiError;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.integrations.DetailedIntegrationData;
import com.namelessmc.java_api.modules.ModuleNames;
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

					final NamelessUser user = api.userByMinecraftUuid(((NamelessPlayer) sender).uuid());
					if (user != null) {
						this.scheduler().runSync(() -> printInfo(sender, user));
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

					NamelessPlayer targetPlayer = this.plugin().audiences().playerByUsername(args[0]);
					if (targetPlayer != null) {
						NamelessUser user = api.userByMinecraftUuid(targetPlayer.uuid());
						if (user == null) {
							sender.sendMessage(language().get(ERROR_TARGET_NO_WEBSITE_ACCOUNT));
							return;
						}
					} else if (args[0].matches(".+#\\d{4}")) {
						// Likely a discord username
						NamelessUser user = api.userByDiscordUsername(args[0]);
						if (user == null) {
							sender.sendMessage(language().get(ERROR_DISCORD_USERNAME_NOT_EXIST));
							return;
						}
					} else {
						try {
							// Maybe a UUID?
							NamelessUser user = api.userByMinecraftUuid(UUID.fromString(args[0]));
							if (user == null) {
								sender.sendMessage(language().get(ERROR_MINECRAFT_UUID_NOT_EXIST));
								return;
							}
							printInfo(sender, user);
						} catch (final IllegalArgumentException e) {
							// Lookup by username
							NamelessUser user = api.userByUsername(args[0]);
							if (user == null) {
								sender.sendMessage(language().get(ERROR_WEBSITE_USERNAME_NOT_EXIST));
								return;
							}
							printInfo(sender, user);
						}
					}
				} catch (NamelessException e) {
					sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
					logger().logException(e);
				}
			});
			return;
		}

		sender.sendMessage(this.usage());
	}

	private void printInfo(final NamelessCommandSender sender,
						   final NamelessUser user) {
		try {
			List<Runnable> runSync = new LinkedList<>();

			String username = user.username();
			String displayName = user.displayName();

			runSync.add(() -> {
				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_USERNAME, "username", username));
				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_DISPLAY_NAME, "displayname", displayName));
			});

			final Group primaryGroup = user.primaryGroup();
			if (primaryGroup != null) {
				runSync.add(() -> {
					sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP,
							"groupname", primaryGroup.getName(),
							"id", String.valueOf(primaryGroup.getId())));
				});
			}

			final String groupCommaString = user.groups().stream().map(Group::getName).collect(Collectors.joining(", "));
			final String registeredDate = this.plugin().dateFormatter().format(user.registeredDate());

			runSync.add(() -> {
				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_ALL_GROUPS,
						"groups_names_list", groupCommaString));
				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_REGISTER_DATE,
						"date", registeredDate));
			});

			boolean verified = user.isVerified();
			boolean banned = user.isBanned();

			runSync.add(() -> {
				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_VALIDATED,
						Placeholder.component("validated", language().booleanText(verified, true))));

				sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_BANNED,
						Placeholder.component("banned", language().booleanText(banned, false))));
			});

			Collection<CustomProfileFieldValue> customFields = user.profileFields();

			runSync.add(() -> {
				for (final CustomProfileFieldValue customField : customFields) {
					String value = customField.value();
					if (value == null) {
						value = "-";
					}
					sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_CUSTOM_FIELD,
							"name", customField.field().name(), "value", value));
				}
			});

			if (user.api().website().modules().contains(ModuleNames.STORE)) {
				try {
					float credits = user.store().credits();
					runSync.add(() -> {
						sender.sendMessage(this.language().get(COMMAND_USERINFO_OUTPUT_STORE_MODULE_CREDITS,
								"credits", String.valueOf(credits)));
					});
				} catch (ApiException e) {
					if (e.apiError() == ApiError.NAMELESS_INVALID_API_METHOD) {
						this.logger().warning("Skipped showing store credits, you are using a store module version that does not have the endpoint yet.");
					} else {
						throw e;
					}
				}
			}

			Map<String, DetailedIntegrationData> integrations = user.integrations();
			if (!integrations.isEmpty()) {
				runSync.add(() -> {
					sender.sendMessage(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_HEADER));
					integrations.forEach((name, data) -> {
						sender.sendMessage(Component.text("  " + name + ":"));
						final Component indent = Component.text("    ");
						sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_IDENTIFIER,
								"identifier", data.identifier())));
						sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_USERNAME,
								"username", data.username())));
						sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_LINKED_DATE,
								"linked_date", this.plugin().dateFormatter().format(data.linkedDate()))));
						sender.sendMessage(indent.append(language().get(COMMAND_USERINFO_OUTPUT_INTEGRATIONS_VERIFIED,
								Placeholder.component("is_verified", language().booleanText(data.isVerified(), true)))));
					});
				});
			}

			this.plugin().scheduler().runSync(() -> runSync.forEach(Runnable::run));
		} catch (NamelessException e) {
			sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
			logger().logException(e);
		}
	}

	@Override
	public List<String> complete(@NonNull NamelessCommandSender sender, @NonNull String @NonNull [] args) {
		if (args.length == 1) {
			return this.plugin().userCache().usernamesSearch(args[0]);
		}
		return Collections.emptyList();
	}

}
