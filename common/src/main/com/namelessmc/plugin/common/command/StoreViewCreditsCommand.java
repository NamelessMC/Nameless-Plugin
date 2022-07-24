package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessConsole;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;

import java.util.Collections;
import java.util.List;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class StoreViewCreditsCommand extends CommonCommand {

	public StoreViewCreditsCommand(NamelessPlugin plugin) {
		super(
				plugin,
				"store-view-credits",
				COMMAND_STORE_VIEW_CREDITS_USAGE,
				COMMAND_STORE_VIEW_CREDITS_DESCRIPTION,
				Permission.COMMAND_STORE_VIEW_CREDITS
		);
	}

	@Override
	public void execute(NamelessCommandSender sender, String[] args) {
		if (args.length > 1) {
			sender.sendMessage(this.usage());
			return;
		}

		if (sender instanceof NamelessConsole && args.length == 0) {
			sender.sendMessage(this.language().get(COMMAND_STORE_VIEW_CREDITS_OUTPUT_CONSOLE_MUST_PROVIDE_TARGET));
			return;
		}

		if (args.length == 1 && !sender.hasPermission(Permission.COMMAND_STORE_VIEW_CREDITS_OTHERS)) {
			sender.sendMessage(this.language().get(COMMAND_STORE_VIEW_CREDITS_OUTPUT_NO_PERMISSION_OTHER));
			return;
		}

		this.scheduler().runAsync(() -> {
			try {
				NamelessAPI api = this.apiProvider().api();
				if (api == null) {
					sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION));
					return;
				}

				NamelessUser user;

				if (args.length == 1) {
					user = api.userByMinecraftUsername(args[0]);
					if (user == null) {
						sender.sendMessage(this.language().get(ERROR_TARGET_NO_WEBSITE_ACCOUNT));
						return;
					}
				} else {
					user = api.userByMinecraftUuid(((NamelessPlayer) sender).uuid());
					if (user == null) {
						sender.sendMessage(this.language().get(PLAYER_SELF_NOT_REGISTERED));
						return;
					}
				}

				float credits = user.store().credits();
				sender.sendMessage(this.language().get(COMMAND_STORE_VIEW_CREDITS_OUTPUT_CREDITS,
						"credits", String.valueOf(credits)));
			} catch (NamelessException e) {
				sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION));
				this.logger().logException(e);
			}
		});
	}

	@Override
	public List<String> complete(final NamelessCommandSender sender, final String[] args) {
		if (args.length == 1) {
			return this.plugin().userCache().minecraftUsernamesSearch(args[0]);
		}
		return Collections.emptyList();
	}

}
