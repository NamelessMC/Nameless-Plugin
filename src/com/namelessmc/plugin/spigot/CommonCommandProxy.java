package com.namelessmc.plugin.spigot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.namelessmc.plugin.common.command.GetNotificationsCommand;
import com.namelessmc.plugin.common.command.RegisterCommand;
import com.namelessmc.plugin.common.command.ReportCommand;
import com.namelessmc.plugin.common.command.UserInfoCommand;
import com.namelessmc.plugin.common.command.VerifyCommand;

public class CommonCommandProxy extends Command {

	static final Map<String, Supplier<CommonCommandProxy>> COMMAND_SUPPLIERS = new HashMap<>();

	static {
		COMMAND_SUPPLIERS.put("get-notifications", () -> createCommand(
				new GetNotificationsCommand(NamelessPlugin.getInstance()),
				"get-notifications",
				Term.COMMAND_NOTIFICATIONS_DESCRIPTION,
				Term.COMMAND_NOTIFICATIONS_USAGE,
				Permission.COMMAND_GET_NOTIFICATIONS));

		COMMAND_SUPPLIERS.put("register", () -> createCommand(
				new RegisterCommand(NamelessPlugin.getInstance()),
				"register",
				Term.COMMAND_REGISTER_DESCRIPTION,
				Term.COMMAND_REGISTER_USAGE,
				Permission.COMMAND_REGISTER));

		COMMAND_SUPPLIERS.put("report", () -> createCommand(
				new ReportCommand(NamelessPlugin.getInstance()),
				"report",
				Term.COMMAND_REPORT_DESCRIPTION,
				Term.COMMAND_REPORT_USAGE,
				Permission.COMMAND_REPORT));

		COMMAND_SUPPLIERS.put("user-info", 	() -> createCommand(
				new UserInfoCommand(NamelessPlugin.getInstance()),
				"user-info",
				Term.COMMAND_USERINFO_DESCRIPTION,
				Term.COMMAND_USERINFO_USAGE,
				Permission.COMMAND_USER_INFO));

		COMMAND_SUPPLIERS.put("verify", () -> createCommand(
				new VerifyCommand(NamelessPlugin.getInstance()),
				"verify",
				Term.COMMAND_VALIDATE_DESCRIPTION,
				Term.COMMAND_VALIDATE_USAGE,
				Permission.COMMAND_VERIFY));
	}

	private final CommonCommand commonCommand;

	private CommonCommandProxy(final CommonCommand commonCommand, final String name, final String description, final String usage, final Permission permission, final List<String> aliases) {
		super(name, description, usage, aliases);
		this.setPermission(permission.toString());
		this.commonCommand = commonCommand;
	}

	private static CommonCommandProxy createCommand(final CommonCommand commonCommand, final String configName, final Term description, final Term usage, final Permission permission) {
		return new CommonCommandProxy(commonCommand,
				Config.COMMANDS.getConfig().getString(configName),
				NamelessPlugin.getInstance().getLanguage().getMessage(description),
				NamelessPlugin.getInstance().getLanguage().getMessage(usage).replace("{command}", Config.COMMANDS.getConfig().getString(configName)),
				permission,
				Collections.emptyList());
	}

	@Override
	public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
		final SpigotCommandSender sender2 = new SpigotCommandSender(sender);

		if (!sender.hasPermission(Objects.requireNonNull(this.getPermission()))) {
			sender2.sendMessage(NamelessPlugin.getInstance().getLanguage().getMessage(Term.COMMAND_NO_PERMISSION));
			return true;
		}

		this.commonCommand.execute(sender2, args, this.getUsage());
		return true;
	}

//	private final String usageMessage;
//	private final Permission permission;
//
//	protected Command(final String name, final Term description, final Term usage, final Permission permission) {
//		super(Config.COMMANDS.getConfig().getString(name),
//				NamelessPlugin.getInstance().getLanguageHandler().getMessage(description),
//				NamelessPlugin.getInstance().getLanguageHandler().getMessage(usage, "command", Config.COMMANDS.getConfig().getString(name)),
//				Collections.emptyList());
//		this.usageMessage = NamelessPlugin.getInstance().getLanguageHandler().getMessage(usage, "command", Config.COMMANDS.getConfig().getString(name));
//		this.permission = permission;
//	}
//
//	@Override
//	public NamelessPlugin getPlugin() {
//		return NamelessPlugin.getInstance();
//	}
//
//	public String getUsageWithoutSlash() {
//		return this.usageMessage;
//	}
//
//	@Override
//	public boolean execute(final CommandSender sender, final String label, final String[] args) {
//		if (!this.permission.hasPermission(sender)) {
//			NamelessPlugin.getInstance().getLanguageHandler().send(Term.COMMAND_NO_PERMISSION, sender);
//			return true;
//		}
//
//		final boolean success = execute(sender, args);
//
//		if (!success) {
//			sender.sendMessage(this.getUsage());
//		}
//
//		return success;
//	}
//
//	public abstract boolean execute(CommandSender sender, String[] args);

}
