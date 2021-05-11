package com.namelessmc.plugin.common.command;

import java.util.Optional;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AccountAlreadyActivatedException;
import com.namelessmc.java_api.exception.InvalidValidateCodeException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;

public class ValidateCommand extends CommonCommand {

	public ValidateCommand(final CommonObjectsProvider provider) {
		super(provider);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if (args.length != 1) {
			sender.sendMessage(getLanguage().getMessage(Term.COMMAND_VALIDATE_USAGE));
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getMessage(Term.COMMAND_NOTAPLAYER));
			return;
		}

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final Optional<NamelessUser> user = NamelessPlugin.getInstance().getNamelessApi().getUser(sender.getUniqueId());
				if (!user.isPresent()) {
					sender.sendMessage(getLanguage().getMessage(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				final String code = args[0];
				user.get().verifyMinecraft(code);
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_VALIDATE_OUTPUT_SUCCESS));
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC));
				e.printStackTrace();
			} catch (final InvalidValidateCodeException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE));
			} catch (final AccountAlreadyActivatedException e) {
				sender.sendMessage(getLanguage().getMessage(Term.COMMAND_VALIDATE_OUTPUT_FAIL_ALREADYVALIDATED));
			}
		});
	}


}
