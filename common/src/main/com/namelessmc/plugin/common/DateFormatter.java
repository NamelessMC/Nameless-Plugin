package com.namelessmc.plugin.common;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements Reloadable {

	private final @NonNull ConfigurationHandler config;

	private DateFormat formatter;

	DateFormatter(final @NonNull ConfigurationHandler config) {
		this.config = config;
	}

	@Override
	public void reload() {
		this.formatter = new SimpleDateFormat(config.main().getString("datetime-format"));
	}

	public String format(final @NonNull Date date) {
		return formatter.format(date);
	}

}
