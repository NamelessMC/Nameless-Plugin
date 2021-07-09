package com.namelessmc.plugin.bungee;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.namelessmc.plugin.common.LanguageHandler.Term;
import com.namelessmc.plugin.common.Permission;
import com.namelessmc.plugin.common.command.CommonCommand;
import com.namelessmc.plugin.common.command.GetNotificationsCommand;
import com.namelessmc.plugin.common.command.RegisterCommand;
import com.namelessmc.plugin.common.command.ReportCommand;
import com.namelessmc.plugin.common.command.UserInfoCommand;
import com.namelessmc.plugin.common.command.ValidateCommand;
import com.namelessmc.plugin.spigot.Config;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommonCommandProxy extends Command {

	static final Map<String, Supplier<CommonCommandProxy>> COMMAND_SUPPLIERS = new HashMap<>();

	static {
		COMMAND_SUPPLIERS.put("get-notifications", () -> createCommand(
				new GetNotificationsCommand(NamelessPlugin.getInstance()),
				"get-notifications",
				Term.COMMAND_NOTIFICATIONS_USAGE,
				Permission.COMMAND_GET_NOTIFICATIONS));

		COMMAND_SUPPLIERS.put("register", () -> createCommand(
				new RegisterCommand(NamelessPlugin.getInstance()),
				"register",
				Term.COMMAND_REGISTER_USAGE,
				Permission.COMMAND_REGISTER));

		COMMAND_SUPPLIERS.put("report", () -> createCommand(
				new ReportCommand(NamelessPlugin.getInstance()),
				"report",
				Term.COMMAND_REPORT_USAGE,
				Permission.COMMAND_REPORT));

		COMMAND_SUPPLIERS.put("user-info", 	() -> createCommand(
				new UserInfoCommand(NamelessPlugin.getInstance()),
				"user-info",
				Term.COMMAND_USERINFO_USAGE,
				Permission.COMMAND_USER_INFO));

		COMMAND_SUPPLIERS.put("validate", () -> createCommand(
				new ValidateCommand(NamelessPlugin.getInstance()),
				"validate",
				Term.COMMAND_VALIDATE_USAGE,
				Permission.COMMAND_VALIDATE));
	};

	private final CommonCommand commonCommand;
	private final String usage;

	private CommonCommandProxy(final CommonCommand commonCommand, final String name, final String usage, final Permission permission, final String[] aliases) {
		super(name, permission.toString(), aliases);
		this.commonCommand = commonCommand;
		this.usage = usage;
	}

	private static CommonCommandProxy createCommand(final CommonCommand commonCommand, final String configName, final Term usage, final Permission permission) {
		return new CommonCommandProxy(commonCommand,
				Config.COMMANDS.getConfig().getString(configName),
				NamelessPlugin.getInstance().getLanguage().getMessage(usage).replace("{command}", Config.COMMANDS.getConfig().getString(configName)),
				permission,
				new String[] {});
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new ComponentBuilder("The bungeecord plugin does not support running commands from the console at this time.").create());
		}

		final BungeeCommandSender sender2 = new BungeeCommandSender((ProxiedPlayer) sender);

//		if (!sender.hasPermission(Objects.requireNonNull(this.getPermission()))) {
//			sender2.sendMessage(NamelessPlugin.getInstance().getLanguage().getMessage(Term.COMMAND_NO_PERMISSION));
//			return;
//		}

		this.commonCommand.execute(sender2, args, this.usage);
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
