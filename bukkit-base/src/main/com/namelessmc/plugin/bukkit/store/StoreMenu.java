package com.namelessmc.plugin.bukkit.store;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.store.StoreProduct;
import com.namelessmc.plugin.common.NamelessPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.derkades.derkutils.bukkit.ItemBuilder;
import xyz.derkades.derkutils.bukkit.menu.IconMenu;
import xyz.derkades.derkutils.bukkit.menu.OptionClickEvent;

import java.util.Collections;
import java.util.List;

public class StoreMenu extends IconMenu {

	private final NamelessPlugin plugin;

	private @Nullable StoreProduct[] productsBySlot;

	public StoreMenu(Plugin bukkitPlugin, NamelessPlugin plugin, Player player) {
		super(bukkitPlugin, "Store menu", 3, player);
		this.plugin = plugin;

		this.plugin.scheduler().runAsync(this::loadPackages);
	}

	public void loadPackages() {
		NamelessAPI api = this.plugin.apiProvider().api();
		if (api == null) {
			return;
		}

		final List<StoreProduct> products;
		try {
			products = api.store().products();
		} catch (NamelessException e) {
			e.printStackTrace();
			return;
		}

		this.plugin.scheduler().runSync(() -> fillMenu(products));
	}

	public void fillMenu(List<StoreProduct> products) {
		StoreProduct[] productsBySlot = new StoreProduct[27];
		int slot = 0;
		for (StoreProduct product : products) {
			if (product.isHidden()) {
				continue;
			}

			ItemStack item = new ItemBuilder(Material.STONE)
					.name(product.name())
					.lore(Collections.singletonList("Price: " + String.format("%.2f", product.priceCents() / 100f)))
					.create();

			this.addItem(slot, item);
			productsBySlot[slot] = product;
			slot++;
		}
	}

	@Override
	public boolean onOptionClick(OptionClickEvent event) {
		int slot = event.getPosition();
		Player player = event.getPlayer();

		if (this.productsBySlot == null) {
			player.sendMessage("menu not loaded yet");
			return false;
		}

		StoreProduct product = this.productsBySlot[slot];

		if (product == null) {
			player.sendMessage("unknown click");
			return false;
		}
		
		if (product.isDisabled()) {
			player.sendMessage("product is disabled");
			return false;
		}

		player.sendMessage("buy product " + product);
		return true;
	}
}
