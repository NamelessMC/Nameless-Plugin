package com.namelessmc.plugin.common;

public interface Reloadable {

	void unload();

	void load();

	public static enum Order {

		FIRST, NORMAL, LAST;

	}

}
