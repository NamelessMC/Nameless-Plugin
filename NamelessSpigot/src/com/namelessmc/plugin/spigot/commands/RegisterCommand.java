package com.namelessmc.plugin.spigot.commands;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.ApiError;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.exception.CannotSendEmailException;
import com.namelessmc.java_api.exception.InvalidUsernameException;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

public class RegisterCommand extends Command {

	public RegisterCommand() {
		super("register",
				Term.COMMAND_REGISTER_DESCRIPTION,
				Term.COMMAND_REGISTER_USAGE,
				Permission.COMMAND_REGISTER);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		if (args.length != 1) {
			return false;
		}

		final LanguageHandler<CommandSender> lang = NamelessPlugin.getInstance().getLanguageHandler();

		if (!(sender instanceof Player)) {
			lang.send(Term.COMMAND_NOTAPLAYER, sender);
			return true;
		}

		final Player player = (Player) sender;

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final Optional<String> link = NamelessPlugin.getInstance().getNamelessApi().registerUser(player.getName(), args[0], Optional.of(player.getUniqueId()));
				if (link.isPresent()) {
					lang.send(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_LINK, player, "link", link.get());
				} else {
					lang.send(Term.COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL, player);
				}
			} catch (final ApiError e) {
				if (e.getError() == ApiError.EMAIL_ALREADY_EXISTS) {
					lang.send(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED, player);
				} else if (e.getError() == ApiError.USERNAME_ALREADY_EXISTS) {
					lang.send(Term.COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS, player);
				} else if (e.getError() == ApiError.INVALID_EMAIL_ADDRESS) {
					lang.send(Term.COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID, player);
				} else {
					lang.send(Term.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC, player);
					e.printStackTrace();
				}
			} catch (final NamelessException e) {
				lang.send(Term.COMMAND_REGISTER_OUTPUT_FAIL_GENERIC, player);
				e.printStackTrace();
			} catch (final InvalidUsernameException e) {
				lang.send(Term.COMMAND_REGISTER_OUTPUT_FAIL_USERNAMEINVALID, player);
			} catch (final CannotSendEmailException e) {
				lang.send(Term.COMMAND_REGISTER_OUTPUT_FAIL_CANNOTSENDEMAIL, player);
			}
		});

		return true;
	}

}