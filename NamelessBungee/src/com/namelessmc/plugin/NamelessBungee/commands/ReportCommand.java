package com.namelessmc.plugin.NamelessBungee.commands;

import java.util.UUID;

import com.namelessmc.NamelessAPI.NamelessException;
import com.namelessmc.NamelessAPI.NamelessPlayer;
import com.namelessmc.plugin.NamelessBungee.Message;
import com.namelessmc.plugin.NamelessBungee.Nameless;
import com.namelessmc.plugin.NamelessBungee.util.UUIDFetcher;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ReportCommand extends Command {

	private String commandName;

	public ReportCommand(String name) {
		super(name);
		commandName = name;
	}

	@SuppressWarnings("unused")
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission(Nameless.PERMISSION + ".report")) {
			sender.sendMessage(Message.NO_PERMISSION.getComponents());
			return;
		}
		
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(Message.MUST_BE_INGAME.getComponents());
			return;
		}
		
		ProxyServer.getInstance().getScheduler().runAsync(Nameless.getInstance(), () -> {
			
			ProxiedPlayer player = (ProxiedPlayer) sender;
			NamelessPlayer namelessPlayer = new NamelessPlayer(player.getUniqueId(), Nameless.baseApiURL);
			
			if (namelessPlayer.exists()) {
				sender.sendMessage(Message.MUST_REGISTER.getComponents());
			}
			
			if (false /* TODO Check if account is verified*/){
				player.sendMessage(Message.PLAYER_NOT_VALID.getComponents());
				return;
			}
						
			if (args.length < 2) {
				player.sendMessage(TextComponent.fromLegacyText(
						Message.INCORRECT_USAGE_REPORT.getMessage().replace("%command%", commandName)));
				return;
			}
			
			final String targetName = args[0];
			final UUID targetUuid;
			
			try {
				targetUuid = UUIDFetcher.getUUID(targetName);
			} catch (IllegalArgumentException e) {
				sender.sendMessage(new ComponentBuilder("This player could not be found").color(ChatColor.RED).create()); // TODO Use messages.yml
				return;
			}
			
			//Remove first argument, all other arguments are part of the reason
			int n = args.length - 1;
			String[] reasonWordsArray = new String[n];
			System.arraycopy(args, 1, reasonWordsArray, 0, n);
			
			String reason = String.join(" ", reasonWordsArray); //Join with space in between words
			
			try {
				namelessPlayer.reportPlayer(targetUuid, targetName, reason);
				
				//Report successful
				sender.sendMessage(TextComponent.fromLegacyText(
						Message.REPORT_SUCCESS.getMessage().replace("%player%", args[0])));
			} catch (NamelessException e) {
				sender.sendMessage(new ComponentBuilder("An error occured: " + e.getMessage()).color(ChatColor.RED).create());
			}
			
		});
	}

}