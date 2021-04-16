package com.namelessmc.plugin.spigot.commands;

import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.AccountAlreadyActivatedException;
import com.namelessmc.java_api.exception.InvalidValidateCodeException;
import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

/**
 * Command used to submit a code to validate a user's NamelessMC account
 */
public class ValidateCommand extends Command {

	public ValidateCommand() {
		super("validate",
				Term.COMMAND_VALIDATE_DESCRIPTION,
				Term.COMMAND_VALIDATE_USAGE,
				Permission.COMMAND_VALIDATE);
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		final LanguageHandler<CommandSender> lang = NamelessPlugin.getInstance().getLanguageHandler();

		if (args.length != 1) {
			return false;
		}

		if (!(sender instanceof Player)) {
			lang.send(Term.COMMAND_NOTAPLAYER, sender);
			return true;
		}

		final Player player = (Player) sender;

		NamelessPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(NamelessPlugin.getInstance(), () -> {
			try {
				final Optional<NamelessUser> user = NamelessPlugin.getInstance().getNamelessApi().getUser(player.getUniqueId());
				if (!user.isPresent()) {
					lang.send(Term.PLAYER_SELF_NOTREGISTERED, sender);
					return;
				}

				final String code = args[0];
				user.get().verifyMinecraft(code);
				lang.send(Term.COMMAND_VALIDATE_OUTPUT_SUCCESS, sender);
			} catch (final NamelessException e) {
				lang.send(Term.COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC, sender);
				e.printStackTrace();
			} catch (final InvalidValidateCodeException e) {
				lang.send(Term.COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE, sender);
			} catch (final AccountAlreadyActivatedException e) {
				lang.send(Term.COMMAND_VALIDATE_OUTPUT_FAIL_ALREADYVALIDATED, sender);
			}
		});

		return true;
	}

}
