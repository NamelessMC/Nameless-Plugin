package com.namelessmc.plugin.common.command;

import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_NOT_A_PLAYER;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_VALIDATE_DESCRIPTION;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_VALIDATE_OUTPUT_FAIL_ALREADY_VALIDATED;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_VALIDATE_OUTPUT_FAIL_INVALID_CODE;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_VALIDATE_OUTPUT_FAIL_MINECRAFT_ACCOUNT_LINKED;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_VALIDATE_OUTPUT_FAIL_OTHERS_ONLY_FROM_CONSOLE;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_VALIDATE_OUTPUT_SUCCESS;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_VALIDATE_USAGE;
import static com.namelessmc.plugin.common.LanguageHandler.Term.ERROR_WEBSITE_CONNECTION;

import org.checkerframework.checker.nullness.qual.NonNull;

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
		if (args.length < 1 || args.length > 2) {
			sender.sendMessage(this.usage());
			return;
		}

		final NamelessPlayer target;
		if (args.length == 2) {
			if (!(sender instanceof NamelessConsole)) {
				sender.sendMessage(this.language().get(COMMAND_VALIDATE_OUTPUT_FAIL_OTHERS_ONLY_FROM_CONSOLE));
				return;
			}
			target = this.plugin().audiences().playerByUsername(args[1]);
		} else {
			if (!(sender instanceof NamelessPlayer)) {
				sender.sendMessage(this.language().get(COMMAND_NOT_A_PLAYER));
				return;
			}
			target = (NamelessPlayer) sender;
		}

		this.scheduler().runAsync(() -> {
			final NamelessAPI api = this.apiProvider().api();
			if (api == null) {
				sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION));
				return;
			}

			try {
				final String code = args[0];

				final IntegrationData integrationData = new MinecraftIntegrationData(target.uuid(), target.username());
				api.verifyIntegration(integrationData, code);
				sender.sendMessage(this.language().get(COMMAND_VALIDATE_OUTPUT_SUCCESS));
				this.plugin().groupSync().resetGroups(target);
			} catch (final ApiException e) {
				switch(e.apiError()) {
					case CORE_INVALID_CODE:
						sender.sendMessage(this.language().get(COMMAND_VALIDATE_OUTPUT_FAIL_INVALID_CODE));
						return;
					case CORE_INTEGRATION_ALREADY_VERIFIED:
						sender.sendMessage(this.language().get(COMMAND_VALIDATE_OUTPUT_FAIL_ALREADY_VALIDATED));
						return;
					case CORE_INTEGRATION_IDENTIFIER_ERROR:
					case CORE_INTEGRATION_USERNAME_ERROR:
						sender.sendMessage(this.language().get(COMMAND_VALIDATE_OUTPUT_FAIL_MINECRAFT_ACCOUNT_LINKED));
						return;
					default:
						sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION));
						this.logger().logException(e);
				}
			} catch (final NamelessException e) {
				sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION));
				this.logger().logException(e);
			}
		});
	}

}
