package com.namelessmc.plugin.NamelessBungee.commands;

import java.util.UUID;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.NamelessPlugin;
import com.namelessmc.plugin.NamelessBungee.util.UUIDFetcher;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class SetGroupCommand extends Command {

	private String commandName;
	
	public SetGroupCommand(String commandName) {
		super(commandName);
		this.commandName = commandName;
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if (!sender.hasPermission(NamelessPlugin.PERMISSION_ADMIN + ".setgroup")) {
			sender.sendMessage(Message.NO_PERMISSION.getComponents());
			return;
		}
		
		if (args.length != 2) {
			sender.sendMessage(TextComponent.fromLegacyText(
					Message.INCORRECT_USAGE_SETGROUP.getMessage().replace("%command%", commandName)));
			return;
		}
		
		final int groupId;
		
		try {
			groupId = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			// The sender has provided a non numeric string
			sender.sendMessage(TextComponent.fromLegacyText(Message.INCORRECT_USAGE_SETGROUP.getMessage().replaceAll("%command%", commandName)));
			return;
		}
		
		
		ProxyServer.getInstance().getScheduler().runAsync(NamelessPlugin.getInstance(), () -> {
			final String targetName = args[0];
			final UUID targetUuuid;
			
			try {
				targetUuuid = UUIDFetcher.getUUID(targetName);
			} catch (IllegalArgumentException e) {
				sender.sendMessage(new ComponentBuilder("This player could not be found").color(ChatColor.RED).create()); // TODO Use messages.yml
				return;
			}
			
			try {
				final NamelessPlayer target = new NamelessPlayer(targetUuuid, NamelessPlugin.baseApiURL);
				final int previousGroupId = target.getGroupID();
				target.setGroup(groupId);
			
				String success = Message.SETGROUP_SUCCESS.getMessage()
						.replace("%player%", args[1])
						.replace("%previousgroup%", String.valueOf(previousGroupId))
						.replace("%newgroup%", String.valueOf(groupId));
				sender.sendMessage(TextComponent.fromLegacyText(success));
			} catch (NamelessException e) {
				sender.sendMessage(new ComponentBuilder("An error occured. See the console log for more details. - " + e.getMessage()).color(ChatColor.RED).create());
				e.printStackTrace();
			}
		}); 
	}

}