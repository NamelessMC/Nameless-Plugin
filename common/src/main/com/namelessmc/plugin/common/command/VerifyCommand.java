package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.integrations.IntegrationData;
import com.namelessmc.java_api.integrations.MinecraftIntegrationData;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class VerifyCommand extends CommonCommand {

	public VerifyCommand(final @NonNull NamelessPlugin plugin) {
		super(plugin,
				"verify",
				COMMAND_VALIDATE_USAGE,
				COMMAND_VALIDATE_DESCRIPTION,
				Permission.COMMAND_VERIFY);
	}

	@Override
	public void execute(final @NonNull NamelessCommandSender sender, final @NonNull String@NonNull[] args) {
		if (args.length != 1) {
			sender.sendMessage(this.usage());
			return;
		}

		if (sender instanceof NamelessConsole) {
			sender.sendMessage(language().get(COMMAND_NOT_A_PLAYER));
			return;
		}

		this.scheduler().runAsync(() -> {
			final NamelessAPI api = this.apiProvider().api();
			if (api == null) {
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				return;
			}

			try {
				final String code = args[0];
				final NamelessPlayer player = (NamelessPlayer) sender;
				final IntegrationData integrationData = new MinecraftIntegrationData(player.uuid(), player.username());
				api.verifyIntegration(integrationData, code);
				sender.sendMessage(language().get(COMMAND_VALIDATE_OUTPUT_SUCCESS));
				this.plugin().groupSync().resetGroups(player);
			} catch (ApiException e) {
				switch(e.apiError()) {
					case CORE_INVALID_CODE:
						sender.sendMessage(language().get(COMMAND_VALIDATE_OUTPUT_FAIL_INVALID_CODE));
						return;
					case CORE_INTEGRATION_ALREADY_VERIFIED:
						sender.sendMessage(language().get(COMMAND_VALIDATE_OUTPUT_FAIL_ALREADY_VALIDATED));
						return;
					case CORE_INTEGRATION_IDENTIFIER_ERROR:
					case CORE_INTEGRATION_USERNAME_ERROR:
						sender.sendMessage(language().get(COMMAND_VALIDATE_OUTPUT_FAIL_MINECRAFT_ACCOUNT_LINKED));
						return;
					default:
						sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
						logger().logException(e);
				}
			} catch (final NamelessException e) {
				sender.sendMessage(language().get(ERROR_WEBSITE_CONNECTION));
				logger().logException(e);
			}
		});
	}

}
