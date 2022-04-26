package com.namelessmc.plugin.sponge;

import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpongeDataSender extends AbstractDataSender {

	protected SpongeDataSender(@NonNull NamelessPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void registerCustomProviders() {}

}
