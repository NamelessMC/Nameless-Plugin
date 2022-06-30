package com.namelessmc.plugin.common;

public interface Reloadable {

	void reload();

	public static enum Order {

		FIRST, NORMAL, LAST;

	}

}
