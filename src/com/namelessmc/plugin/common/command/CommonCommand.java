package com.namelessmc.plugin.common.command;

import java.util.Optional;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import com.namelessmc.plugin.common.ExceptionLogger;
import com.namelessmc.plugin.common.LanguageHandler;
import org.jetbrains.annotations.NotNull;

public abstract class CommonCommand {

	private final CommonObjectsProvider provider;

	public CommonCommand(final CommonObjectsProvider provider) {
		this.provider = provider;
	}

	protected @NotNull AbstractScheduler getScheduler() {
		return this.provider.getScheduler();
	}

	protected @NotNull LanguageHandler getLanguage() {
		return this.provider.getLanguage();
	}

	protected @NotNull Optional<NamelessAPI> getApi(){
		return this.provider.getApiProvider().getNamelessApi();
	}

	protected @NotNull ExceptionLogger getExceptionLogger() { return this.provider.getExceptionLogger(); }

	protected boolean useUuids() {
		return this.provider.getApiProvider().useUuids();
	}

	public abstract void execute(CommandSender sender, String[] args, String usage);

}
