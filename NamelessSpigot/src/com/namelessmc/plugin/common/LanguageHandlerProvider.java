package com.namelessmc.plugin.common;

public interface LanguageHandlerProvider<MessageReceiver> {

	LanguageHandler<MessageReceiver> getLanguageHandler();

}
