package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AccountAlreadyActivatedException;
import com.namelessmc.java_api.exception.InvalidValidateCodeException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;

import java.util.Optional;

public class VerifyCommand extends CommonCommand {

	public VerifyCommand(final CommonObjectsProvider provider) {
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
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final Optional<NamelessUser> user = super.useUuids() ? api.getUser(sender.getUniqueId()) : api.getUser(sender.getName());
				if (!user.isPresent()) {
					sender.sendMessage(getLanguage().getComponent(Term.PLAYER_SELF_NOTREGISTERED));
					return;
				}

				final String code = args[0];
				user.get().verifyMinecraft(code);
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_SUCCESS));
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC));
				e.printStackTrace();
			} catch (final InvalidValidateCodeException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE));
			} catch (final AccountAlreadyActivatedException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_FAIL_ALREADYVALIDATED));
			}
		});
	}


}
