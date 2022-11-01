package com.namelessmc.plugin.common;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements Reloadable {

	private final ConfigurationHandler config;

	private @Nullable DateFormat formatter;

	DateFormatter(final ConfigurationHandler config) {
		this.config = config;
	}

	@Override
	public void unload() {
		this.formatter = null;
	}

	@Override
	public void load() {
		final String format = config.main().node("datetime-format").getString();
		if (format != null) {
			this.formatter = new SimpleDateFormat(format);
		} else {
			this.formatter = new SimpleDateFormat();
		}
	}

	public String format(final Date date) {
		if (this.formatter == null) {
			throw new IllegalStateException("Cannot format dates before DateFormatter is initialized");
		}
		return formatter.format(date);
	}

}
