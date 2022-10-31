package com.namelessmc.plugin.common.command;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.suggestions.Suggestion;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.audiences.NamelessPlayer;

import static com.namelessmc.plugin.common.LanguageHandler.Term.*;

public class SuggestCommand extends CommonCommand {

	public SuggestCommand(NamelessPlugin plugin) {
		super(
				plugin,
				"suggest",
				COMMAND_SUGGEST_USAGE,
				COMMAND_SUGGEST_DESCRIPTION,
				Permission.COMMAND_SUGGEST
		);
	}

	@Override
	protected void execute(NamelessCommandSender sender, String[] args) {
		final String suggestionTitle = String.join(" ", args);
		if (suggestionTitle.length() < 6) {
			sender.sendMessage(this.language().get(COMMAND_SUGGEST_OUTPUT_TOO_SHORT));
			return;
		}

		NamelessAPI api = this.apiProvider().api();

		if (api == null) {
			sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION));
			return;
		}

		if (!(sender instanceof NamelessPlayer)) {
			sender.sendMessage(this.language().get(COMMAND_NOT_A_PLAYER));
			return;
		}

		this.scheduler().runAsync(() -> {
			try {
				NamelessUser user = api.userByMinecraftUuid(((NamelessPlayer) sender).uuid());
				Suggestion suggestion = user.suggestions().createSuggestion(suggestionTitle, suggestionTitle);
				String url = suggestion.url().toString();
				this.scheduler().runSync(() ->
						sender.sendMessage(this.language().get(COMMAND_SUGGEST_OUTPUT_SUCCESS, "url", url)));
			} catch (NamelessException e) {
				this.scheduler().runSync(() ->
						sender.sendMessage(this.language().get(ERROR_WEBSITE_CONNECTION)));
			}
		});
	}

}
