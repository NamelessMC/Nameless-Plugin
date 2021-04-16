package com.namelessmc.plugin.spigot.commands;

import java.util.Collections;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.spigot.Config;
import com.namelessmc.plugin.spigot.NamelessPlugin;
import com.namelessmc.plugin.spigot.Permission;

public abstract class Command extends org.bukkit.command.Command implements PluginIdentifiableCommand {

	public static final Command[] COMMANDS = {
			new GetNotificationsCommand(),
			new RegisterCommand(),
			new ReportCommand(),
			new UserInfoCommand(),
			new ValidateCommand(),
	};

	private final String usageMessage;
	private final Permission permission;

	protected Command(final String name, final Term description, final Term usage, final Permission permission) {
		super(Config.COMMANDS.getConfig().getString(name),
				NamelessPlugin.getInstance().getLanguageHandler().getMessage(description),
				NamelessPlugin.getInstance().getLanguageHandler().getMessage(usage, "command", Config.COMMANDS.getConfig().getString(name)),
				Collections.emptyList());
		this.usageMessage = NamelessPlugin.getInstance().getLanguageHandler().getMessage(usage, "command", Config.COMMANDS.getConfig().getString(name));
		this.permission = permission;
	}

	@Override
	public NamelessPlugin getPlugin() {
		return NamelessPlugin.getInstance();
	}

	public String getUsageWithoutSlash() {
		return this.usageMessage;
	}

	@Override
	public boolean execute(final CommandSender sender, final String label, final String[] args) {
		if (!this.permission.hasPermission(sender)) {
			NamelessPlugin.getInstance().getLanguageHandler().send(Term.COMMAND_NO_PERMISSION, sender);
			return true;
		}

		final boolean success = execute(sender, args);

		if (!success) {
			sender.sendMessage(this.getUsage());
		}

		return success;
	}

	public abstract boolean execute(CommandSender sender, String[] args);

}