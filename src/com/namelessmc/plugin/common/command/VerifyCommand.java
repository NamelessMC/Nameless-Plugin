package com.namelessmc.plugin.common.command;

import java.util.Optional;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AccountAlreadyActivatedException;
import com.namelessmc.java_api.exception.InvalidValidateCodeException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;

public class VerifyCommand extends CommonCommand {

	public VerifyCommand(final CommonObjectsProvider provider) {
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

		final Optional<NamelessAPI> optApi = this.getApi();
		if (!optApi.isPresent()) {
			sender.sendMessage(this.getLanguage().getMessage(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
			return;
		}
		final NamelessAPI api = optApi.get();

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final Optional<NamelessUser> user = api.getUser(sender.getUniqueId());
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
