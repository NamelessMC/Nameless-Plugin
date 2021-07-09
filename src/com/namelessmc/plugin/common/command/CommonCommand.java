package com.namelessmc.plugin.common.command;

import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler;

public abstract class CommonCommand {

	private final AbstractScheduler scheduler;
	private final LanguageHandler language;

	public CommonCommand(final CommonObjectsProvider provider) {
		this.scheduler = provider.getScheduler();
		this.language = provider.getLanguage();
	}

	protected AbstractScheduler getScheduler() {
		return this.scheduler;
	}

	protected LanguageHandler getLanguage() {
		return this.language;
	}

	public abstract void execute(CommandSender sender, String[] args, String usage);

}
