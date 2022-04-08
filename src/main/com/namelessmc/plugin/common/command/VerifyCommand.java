package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.InvalidValidateCodeException;
import com.namelessmc.java_api.integrations.IntegrationData;
import com.namelessmc.java_api.integrations.MinecraftIntegrationData;
import com.namelessmc.plugin.common.*;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VerifyCommand extends CommonCommand {

	public VerifyCommand(final @NotNull NamelessPlugin plugin) {
		super(plugin,
				"verify",
				Term.COMMAND_VALIDATE_USAGE,
				Term.COMMAND_VALIDATE_DESCRIPTION,
				Permission.COMMAND_VERIFY);
	}

	@Override
	public void execute(final @NotNull NamelessCommandSender sender, final @NotNull String@NotNull[] args) {
		if (args.length != 1) {
			sender.sendMessage(this.getUsage());
			return;
		}

		if (sender instanceof NamelessConsole) {
			sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTAPLAYER));
			return;
		}

		this.getScheduler().runAsync(() -> {
			final Optional<NamelessAPI> optApi = this.getApi();
			if (optApi.isEmpty()) {
				sender.sendMessage(getLanguage().getComponent(Term.COMMAND_NOTIFICATIONS_OUTPUT_FAIL));
				return;
			}
			final NamelessAPI api = optApi.get();

			try {
				final String code = args[0];
				final NamelessPlayer player = (NamelessPlayer) sender;
				final IntegrationData integrationData = new MinecraftIntegrationData(player.getUniqueId(), player.getUsername());
				api.verifyIntegration(integrationData, code);
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
