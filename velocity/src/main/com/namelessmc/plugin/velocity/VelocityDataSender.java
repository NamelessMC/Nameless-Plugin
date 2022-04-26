package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.AbstractDataSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public class VelocityDataSender extends AbstractDataSender {

	protected VelocityDataSender(final @NonNull NamelessPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void registerCustomProviders() {}

}
