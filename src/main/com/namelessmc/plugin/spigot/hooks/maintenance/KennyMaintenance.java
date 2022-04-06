package com.namelessmc.plugin.spigot.hooks.maintenance;

import eu.kennytv.maintenance.api.MaintenanceProvider;

public class KennyMaintenance implements MaintenanceStatusProvider {
	
	public KennyMaintenance() {
	
	}

	@Override
	public boolean maintenanceEnabled() {
		return MaintenanceProvider.get().isMaintenance();
	}
	
}
