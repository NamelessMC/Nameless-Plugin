package com.namelessmc.plugin.sponge7.audiences;

import com.namelessmc.plugin.common.audiences.NamelessConsole;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.spongepowered.api.Sponge;

public class SpongeNamelessConsole extends NamelessConsole {

	public SpongeNamelessConsole(SpongeAudiences audiences) {
		super(audiences.console());
	}

	@Override
	public void dispatchCommand(String command) {
		Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
	}

}
