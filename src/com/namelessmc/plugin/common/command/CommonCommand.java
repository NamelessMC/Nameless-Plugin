package com.namelessmc.plugin.common.command;

import java.util.Optional;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.LanguageHandler;

public abstract class CommonCommand {

	private final CommonObjectsProvider provider;

	public CommonCommand(final CommonObjectsProvider provider) {
		this.provider = provider;
	}

	protected AbstractScheduler getScheduler() {
		return this.provider.getScheduler();
	}

	protected LanguageHandler getLanguage() {
		return this.provider.getLanguage();
	}

	protected Optional<NamelessAPI> getApi(){
		return this.provider.getNamelessApi();
	}

	public abstract void execute(CommandSender sender, String[] args, String usage);

}
