package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.*;
import com.namelessmc.java_api.integrations.IntegrationData;
import com.namelessmc.java_api.integrations.MinecraftIntegrationData;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.NamelessCommandSender;
import com.namelessmc.plugin.common.NamelessPlayer;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RegisterCommand extends CommonCommand {

	public RegisterCommand(final @NotNull NamelessPlugin plugin) {
		super(plugin,
				"register",
				Term.COMMAND_REGISTER_USAGE,
				Term.COMMAND_REGISTER_DESCRIPTION,
				Permission.COMMAND_REGISTER);
	}

	@Override
	public void execute(final @NotNull NamelessCommandSender sender, final @NotNull String@NotNull[] args) {
		if (args.length != 2) {
			sender.sendMessage(this.getUsage());
			return;
		}

		final String username = args[0];
		final String email = args[1];

		this.getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (optApi.isEmpty()) {
				sender.sendMessage(this.getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				return;
			}

			final NamelessAPI api = optApi.get();

			try {
				Optional<String> link;
				if (sender instanceof NamelessPlayer) {
					final NamelessPlayer player = (NamelessPlayer) sender;
					IntegrationData integrationData = new MinecraftIntegrationData(player.getUniqueId(), player.getUsername());
					link = api.registerUser(username, email, integrationData);
				} else {
					link = api.registerUser(username, email);
				}

				if (link.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_LINK, "url", link.get()));
				} else {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL));
				}
			} catch (final InvalidUsernameException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_INVALID));
			} catch (final CannotSendEmailException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_CANNOT_SEND_EMAIL));
			} catch (final UsernameAlreadyExistsException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_USED));
			} catch (final InvalidEmailAddressException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_INVALID));
			} catch (final EmailAlreadyUsedException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_USED));
			} catch (final IntegrationIdAlreadyExistsException | IntegrationUsernameAlreadyExistsException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_MINECRAFT_USED));
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.ERROR_WEBSITE_CONNECTION));
				getLogger().logException(e);
			}
		});
	}

}
