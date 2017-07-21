package com.namelessmc.namelessplugin.sponge.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.namelessmc.namelessplugin.sponge.NamelessPlugin;
import com.namelessmc.namelessplugin.sponge.utils.RequestUtil;

/*
 *  Register CMD
 */

public class GetNotificationsCommand implements CommandExecutor {

	NamelessPlugin plugin;
	String permission;

	/*
	 *  Constructer
	 */
	public GetNotificationsCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
		this.permission = plugin.permission;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		// check if player has permission Permission & ensure who inputted command is a Player
		if(src instanceof Player && src.hasPermission(permission + ".notifications")){

			Player player = (Player) src;

			// Try to register user
			Task.builder().execute(new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(!(ctx.toString().length() == 0)){
						player.sendMessage(Text.of(TextColors.RED, "Incorrect usage: /getnotifications"));
						return;
					}

					RequestUtil request = new RequestUtil(plugin);
					try {
						request.getNotifications(player.getUniqueId());
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}).submit(plugin);

		} else if (!src.hasPermission(permission + ".notifications")) {
			src.sendMessage(Text.of(TextColors.RED, "You don't have permission to this command!"));
		} else {
			// User must be ingame to use register command
			src.sendMessage(Text.of("You must be ingame to use this command."));
		}
		return CommandResult.success();
	}
}