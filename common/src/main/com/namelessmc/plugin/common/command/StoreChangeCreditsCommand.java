package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;
import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_STORE_CHANGE_CREDITS_OUTPUT_REMOVED;

public class StoreChangeCreditsCommand extends CommonCommand {

	public StoreChangeCreditsCommand(final @NonNull NamelessPlugin plugin) {
		super(plugin,
				"store-change-credits",
				COMMAND_STORE_CHANGE_CREDITS_USAGE,
				COMMAND_STORE_CHANGE_CREDITS_DESCRIPTION,
				Permission.COMMAND_STORE_CHANGE_CREDITS);
	}

	@Override
	public void execute(final NamelessCommandSender sender, final String[] args) {
		if (args.length != 2) {
			sender.sendMessage(this.usage());
			return;
		}

		final float credits;
		try {
			credits = Float.parseFloat(args[1]);

			if (Float.isNaN(credits) || Float.isInfinite(credits)) {
				sender.sendMessage(this.usage());
				return;
			}
		} catch (NumberFormatException e) {
			sender.sendMessage(this.usage());
			return;
		}

		if (credits == 0) {
			sender.sendMessage(this.language().get(COMMAND_STORE_CHANGE_CREDITS_OUTPUT_NOTHING));
			return;
		}

		this.plugin().scheduler().runAsync(() -> {
			final NamelessAPI api = this.apiProvider().api();;

			if (api == null) {
				this.scheduler().runSync(() -> sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION)));
				return;
			}

			try {
				final NamelessUser user = api.userByMinecraftUsername(args[0]);

				if (user == null) {
					this.scheduler().runSync(() -> sender.sendMessage(this.language().get(ERROR_TARGET_NO_WEBSITE_ACCOUNT)));
					return;
				}

				final String namelessUsername = user.username();

				if (credits > 0) {
					user.store().addCredits(credits);
					sender.sendMessage(this.language().get(COMMAND_STORE_CHANGE_CREDITS_OUTPUT_ADDED,
							"credits", String.format("%.2f", credits), "username", namelessUsername));
				} else if (credits < 0) {
					user.store().removeCredits(-credits);
					sender.sendMessage(this.language().get(COMMAND_STORE_CHANGE_CREDITS_OUTPUT_REMOVED,
							"credits", String.format("%.2f", -credits), "username", namelessUsername));
				} else {
					throw new IllegalStateException();
				}
			} catch (NamelessException e) {
				this.logger().logException(e);
			}
		});

	}

}
