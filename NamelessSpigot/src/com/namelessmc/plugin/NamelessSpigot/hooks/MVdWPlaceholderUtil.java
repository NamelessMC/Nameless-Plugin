package com.namelessmc.plugin.NamelessSpigot.hooks;

import com.namelessmc.plugin.NamelessSpigot.NamelessPlugin;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public class MVdWPlaceholderUtil {

	public void hook() {
		PlaceholderAPI.registerPlaceholder(NamelessPlugin.getInstance(), "nameless_alerts", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
				//NamelessPlayer namelessPlayer = new NamelessPlayer(event.getPlayer().getUniqueId(), NamelessPlugin.baseApiURL);
				//if (namelessPlayer.exists()) {
					//try {
						
						//return Integer.toString(namelessPlayer.getAlertCount());
					//} catch (NamelessException e) {
					//	e.printStackTrace();
					//}
					return "disabled";
				//} else {
				//	return "0";
				//}
			}
		});
		PlaceholderAPI.registerPlaceholder(NamelessPlugin.getInstance(), "nameless_messages", new PlaceholderReplacer() {
			@Override
			public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
				//NamelessPlayer namelessPlayer = new NamelessPlayer(event.getPlayer().getUniqueId(), NamelessPlugin.baseApiURL);
				//if (namelessPlayer.exists()) {
					//try {
					//	//return Integer.toString(namelessPlayer.getMessageCount());
					//} catch (NamelessException e) {
					//	e.printStackTrace();
					//}
					return "disabled";
				//} else {
				//	return "0";
				//}
			}
		});
	}

}