package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.*;
import com.namelessmc.java_api.integrations.IntegrationData;
import com.namelessmc.java_api.integrations.MinecraftIntegrationData;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class RegisterCommand extends CommonCommand {

	public RegisterCommand(final @NonNull NamelessPlugin plugin) {
		super(plugin,
				"register",
				COMMAND_REGISTER_USAGE,
				COMMAND_REGISTER_DESCRIPTION,
				Permission.COMMAND_REGISTER);
	}

	@Override
	public void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		if (args.length != 2) {
			sender.sendMessage(this.usage());
			return;
		}

		final String username = args[0];
		final String email = args[1];

		this.scheduler().runAsync(() -> {
			final NamelessAPI api = this.apiProvider().api();
			if (api == null) {
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				return;
			}

			try {
				Optional<String> link;
				if (sender instanceof NamelessPlayer) {
					final NamelessPlayer player = (NamelessPlayer) sender;
					IntegrationData integrationData = new MinecraftIntegrationData(player.uuid(), player.username());
					link = api.registerUser(username, email, integrationData);
				} else {
					link = api.registerUser(username, email);
				}

				if (link.isPresent()) {
					sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_SUCCESS_LINK, "url", link.get()));
				} else {
					sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL));
				}
			} catch (final InvalidUsernameException e) {
				sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_INVALID));
			} catch (final CannotSendEmailException e) {
				sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_CANNOT_SEND_EMAIL));
			} catch (final UsernameAlreadyExistsException e) {
				sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_USED));
			} catch (final InvalidEmailAddressException e) {
				sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_INVALID));
			} catch (final EmailAlreadyUsedException e) {
				sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_USED));
			} catch (final IntegrationIdentifierInvalidException | IntegrationUsernameInvalidException e) {
				sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_MINECRAFT_USED));
			} catch (final NamelessException e) {
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				logger().logException(e);
			}
		});
	}

}
