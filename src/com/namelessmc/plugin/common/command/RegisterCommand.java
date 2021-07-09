package com.namelessmc.plugin.common.command;

import java.util.Optional;

import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.CannotSendEmailException;
import com.namelessmc.java_api.exception.InvalidUsernameException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;

public class RegisterCommand extends CommonCommand {

	public RegisterCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args, final String usage) {
		if (args.length != 1) {
			sender.sendMessage(usage);
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTAPLAYER));
			return;
		}

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final Optional<String> link = NamelessPlugin.getInstance().getNamelessApi().registerUser(sender.getName(), args[0], Optional.of(sender.getUniqueId()));
				if (link.isPresent()) {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_LINK), "url", link.get());
				} else {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL));
				}
			} catch (final ApiError e) {
				if (e.getError() == ApiError.EMAIL_ALREADY_EXISTS) {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED));
				} else if (e.getError() == ApiError.USERNAME_ALREADY_EXISTS) {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS));
				} else if (e.getError() == ApiError.INVALID_EMAIL_ADDRESS) {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID));
				} else {
					sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC));
					e.printStackTrace();
				}
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC));
				e.printStackTrace();
			} catch (final InvalidUsernameException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_FAIL_USERNAMEINVALID));
			} catch (final CannotSendEmailException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_REGISTER_OUTPUT_FAIL_CANNOTSENDEMAIL));
			}
		});

		return;
	}

}
