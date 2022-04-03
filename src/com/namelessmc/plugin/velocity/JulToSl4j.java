package com.namelessmc.plugin.velocity;

import java.util.logging.Logger;

public class JulToSl4j extends Logger {
	protected JulToSl4j(String name, String resourceBundleName) {
		super(name, resourceBundleName);
	}
}
