package com.namelessmc.plugin.common;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements Reloadable {

	private final @NotNull ConfigurationHandler config;

	private DateFormat formatter;

	DateFormatter(final @NotNull ConfigurationHandler config) {
		this.config = config;
	}

	@Override
	public void reload() {
		this.formatter = new SimpleDateFormat(config.main().getString("datetime-format"));
	}

	public String format(final @NotNull Date date) {
		return formatter.format(date);
	}

}
