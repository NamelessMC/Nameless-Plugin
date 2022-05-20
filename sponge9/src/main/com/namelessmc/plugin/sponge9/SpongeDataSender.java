package com.namelessmc.plugin.sponge9;

import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import org.jetbrains.annotations.NotNull;

public class SpongeDataSender extends AbstractDataSender {

	protected SpongeDataSender(@NotNull NamelessPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void registerCustomProviders() {}

}
