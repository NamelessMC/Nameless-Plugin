package com.namelessmc.plugin.sponge10;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

@Plugin("namelessmc")
public class SpongeNamelessPlugin extends com.namelessmc.plugin.sponge9.SpongeNamelessPlugin {

	@Inject
	public SpongeNamelessPlugin(final @ConfigDir(sharedRoot = false) Path dataDirectory,
								final Logger logger,
								final PluginContainer container) {
		super(dataDirectory, logger, container);
	}

}
