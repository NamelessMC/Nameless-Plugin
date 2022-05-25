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
			} catch (final NamelessException e) {
				if (e instanceof ApiException) {
					switch(((ApiException) e).apiError()) {
						case CORE_INVALID_USERNAME:
							sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_INVALID));
							return;
						case CORE_UNABLE_TO_SEND_REGISTRATION_EMAIL:
							sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_CANNOT_SEND_EMAIL));
							return;
						case CORE_USERNAME_ALREADY_EXISTS:
							sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_USED));
							return;
						case CORE_INVALID_EMAIL_ADDRESS:
							sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_INVALID));
							return;
						case CORE_EMAIL_ALREADY_EXISTS:
							sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_USED));
							return;
					}

					// TODO Reimplement integration api errors
//					sender.sendMessage(language().get(COMMAND_REGISTER_OUTPUT_FAIL_MINECRAFT_USED));
				}
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				logger().logException(e);
			}
		});
	}

}
