package com.namelessmc.plugin.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

public class PropertiesManager implements Reloadable {

	private final NamelessPlugin plugin;
	private final Path propertiesPath;
	private final Properties properties;
	private final Map<String, Supplier<String>> defaultProperties;

	PropertiesManager(Path propertiesPath, NamelessPlugin plugin) {
		this.propertiesPath = propertiesPath;
		this.plugin = plugin;
		this.properties = new Properties();
		this.defaultProperties = new HashMap<>();
	}

	@Override
	public void reload() {
		properties.clear();

		if (Files.isRegularFile(this.propertiesPath)) {
			// Properties file already exists, load it
			this.load();
		}

		// Ensure it contains all required properties
		// If the properties file didn't exist, no properties will exist
		boolean dirty = false;
		for (Map.Entry<String, Supplier<String>> property : this.defaultProperties.entrySet()) {
			if (!properties.containsKey(property.getKey())) {
				this.plugin.logger().warning("Properties file was missing property " + property.getKey());
				properties.setProperty(property.getKey(), property.getValue().get());
				dirty = true;
			}
		}

		if (dirty) {
			this.store();
		}
	}

	private void load() {
		try (InputStream in = Files.newInputStream(this.propertiesPath)) {
			this.properties.clear();
			this.properties.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void store() {
		try (OutputStream out = Files.newOutputStream(this.propertiesPath)) {
			this.properties.store(out, "Please do not modify or delete this file.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void registerProperty(String propertyName, Supplier<String> defaultValueSupplier) {
		this.defaultProperties.put(propertyName, defaultValueSupplier);
	}

	public String get(String propertyName) {
		if (!this.properties.containsKey(propertyName)) {
			throw new IllegalStateException("Property file does not contain property " + propertyName);
		}

		return this.properties.getProperty(propertyName);
	}

	public void set(String propertyName, String propertyValue) {
		this.properties.setProperty(propertyName, propertyValue);
		this.store();
	}

}
