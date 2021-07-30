package com.namelessmc.plugin.common;

import java.util.Optional;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.plugin.common.command.AbstractScheduler;

import net.kyori.adventure.platform.AudienceProvider;

public interface CommonObjectsProvider {

	AbstractScheduler getScheduler();

	LanguageHandler getLanguage();

	Optional<NamelessAPI> getNamelessApi();

	AudienceProvider adventure();

}
