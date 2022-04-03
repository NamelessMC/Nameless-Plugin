package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.InvalidValidateCodeException;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VerifyCommand extends CommonCommand {

	public VerifyCommand(final @NotNull CommonObjectsProvider provider) {
		super(provider,
				"verify",
				Term.COMMAND_VALIDATE_USAGE,
				Permission.COMMAND_VERIFY);
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if (args.length != 1) {
			sender.sendMessage(this.getUsage());
			return;
		}

		if (!sender.isPlayer()) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
			return;
		}

		this.getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (!optApi.isPresent()) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final String code = args[0];
				api.verifyMinecraft(code, sender.getUniqueId(), sender.getName());
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_SUCCESS));
			} catch (final InvalidValidateCodeException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE));
			} catch (final NamelessException e) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC));
				getLogger().logException(e);
			}
		});
	}

}
