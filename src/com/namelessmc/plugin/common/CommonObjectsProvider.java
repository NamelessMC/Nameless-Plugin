package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.command.AbstractScheduler;

import net.kyori.adventure.platform.AudienceProvider;

public interface CommonObjectsProvider {

	AbstractScheduler getScheduler();

	LanguageHandler getLanguage();

	ApiProvider getApiProvider();

	AudienceProvider adventure();

	ExceptionLogger getExceptionLogger();

}
