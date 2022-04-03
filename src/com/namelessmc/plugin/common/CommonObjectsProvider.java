package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.logger.AbstractLogger;

public interface CommonObjectsProvider {

	AbstractScheduler getScheduler();

	LanguageHandler getLanguage();

	ApiProvider getApiProvider();

	AbstractYamlFile getCommandsConfig();

	AbstractLogger getCommonLogger();

}
