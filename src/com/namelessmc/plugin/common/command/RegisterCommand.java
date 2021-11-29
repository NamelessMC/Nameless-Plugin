package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.CannotSendEmailException;
import com.namelessmc.java_api.exception.InvalidUsernameException;
import com.namelessmc.java_api.exception.UsernameAlreadyExistsException;
import com.namelessmc.java_api.exception.UuidAlreadyExistsException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;

import java.util.Optional;

public class RegisterCommand extends CommonCommand {

	public RegisterCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args, final String usage) {
		if (args.length != 1) {
			sender.sendLegacyMessage(usage);
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
			return;
		}

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(this.getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final Optional<String> link =
						super.useUuids() ?
						api.registerUser(sender.getName(), args[0], sender.getUniqueId()) :
						api.registerUser(sender.getName(), args[0]);
				if (link.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_LINK, "url", link.get()));
				} else {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL));
				}
			} catch (final ApiError e) {
				// TODO all these API errors should be converted to thrown exceptions in the java api
				if (e.getError() == ApiError.EMAIL_ALREADY_EXISTS) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED));
				} else if (e.getError() == ApiError.INVALID_EMAIL_ADDRESS) {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID));
				} else {
					sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC));
					e.printStackTrace();
				}
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC));
				e.printStackTrace();
			} catch (final InvalidUsernameException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_USERNAMEINVALID));
			} catch (final CannotSendEmailException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_CANNOTSENDEMAIL));
			} catch (final UuidAlreadyExistsException | UsernameAlreadyExistsException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS));
			}
		});
	}

}
