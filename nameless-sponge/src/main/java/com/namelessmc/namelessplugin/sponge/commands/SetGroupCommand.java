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

public class SetGroupCommand implements CommandExecutor {

	NamelessPlugin plugin;
	String permissionAdmin;

	/*
	 *  Constructer
	 */
	public SetGroupCommand(NamelessPlugin pluginInstance) {
		this.plugin = pluginInstance;
		this.permissionAdmin = plugin.permissionAdmin;
	}

	/*
	 *  Handle inputted command
	 */
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		// check if player has permission Permission & ensure who inputted command is a Player
		if(src.hasPermission(permissionAdmin + ".setgroup")){

			Player player = (Player) src;

			// Try to register user
			Task.builder().execute(new Runnable(){
				@Override
				public void run(){
					// Ensure email is set
					if(!ctx.<String>getOne(Text.of("player")).isPresent() || !ctx.<String>getOne(Text.of("groupId")).isPresent()){
						player.sendMessage(Text.of(TextColors.RED, "Incorrect usage: /setgroup player groupId"));
						return;
					}

					RequestUtil request = new RequestUtil(plugin);
					try {
						request.setGroup(ctx.<String>getOne(Text.of("player")).get(), ctx.<String>getOne(Text.of("groupId")).get());;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).submit(plugin);

		} else if (!src.hasPermission(permissionAdmin + ".setgroup")) {
			src.sendMessage(Text.of(TextColors.RED, "You don't have permission to this command!"));
		}

		return CommandResult.success();
	}
}