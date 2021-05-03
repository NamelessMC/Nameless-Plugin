package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.plugin.common.command.AbstractScheduler;

import net.kyori.adventure.platform.AudienceProvider;

public interface CommonObjectsProvider {

	AbstractScheduler getScheduler();

	LanguageHandler getLanguage();

	NamelessAPI getNamelessApi() throws NamelessException;

	AudienceProvider adventure();

}
