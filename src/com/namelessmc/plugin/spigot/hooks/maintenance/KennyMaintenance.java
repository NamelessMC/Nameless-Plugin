package com.namelessmc.plugin.spigot.hooks.maintenance;

import eu.kennytv.maintenance.api.IMaintenance;
import eu.kennytv.maintenance.api.spigot.MaintenanceSpigotAPI;

public class KennyMaintenance implements MaintenanceStatusProvider {
	
	private final IMaintenance api = MaintenanceSpigotAPI.getAPI();
	
	public KennyMaintenance() {
	
	}

	@Override
	public boolean maintenanceEnabled() {
		return api.isMaintenance();
	}
	
}
