package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessVersion;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class GlobalConstants {

	public static final Set<NamelessVersion> SUPPORTED_WEBSITE_VERSIONS = EnumSet.of(
			NamelessVersion.V2_0_0_PR_13
	);

	public static final Set<NamelessVersion> DEPRECATED_WEBSITE_VERSIONS = Collections.emptySet();

}
