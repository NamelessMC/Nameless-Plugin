package com.namelessmc.plugin.spigot;

import com.namelessmc.plugin.bukkit.BukkitDataSender;
import com.namelessmc.plugin.bukkit.BukkitNamelessPlugin;
import com.namelessmc.plugin.common.NamelessPlugin;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SpigotDataSender extends BukkitDataSender {

    protected SpigotDataSender(@NonNull NamelessPlugin plugin, @NonNull BukkitNamelessPlugin bukkitPlugin) {
        super(plugin, bukkitPlugin);
    }

    @Override
    protected void registerCustomProviders() {
        super.registerCustomProviders();

        this.registerGlobalInfoProvider(json -> {
            json.addProperty("motd", Bukkit.getServer().getMotd());
        });
    }

}
