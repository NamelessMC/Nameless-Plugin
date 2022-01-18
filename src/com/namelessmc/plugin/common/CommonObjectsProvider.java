package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.command.AbstractScheduler;

public interface CommonObjectsProvider {

	AbstractScheduler getScheduler();

	LanguageHandler getLanguage();

	ApiProvider getApiProvider();

	ExceptionLogger getExceptionLogger();

}
