package com.namelessmc.plugin.spigot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public enum Message {

	PLAYER_OTHER_NOTFOUND("player.other.not-found",
			"This player could not be found."),
	PLAYER_OTHER_NOTVALIDATED("player.other.not-validated",
			"This player's account is not validated."),
	PLAYER_OTHER_NOTREGISTERED("player.other.not-registered",
			"This player is not registered on the website."),
	PLAYER_SELF_NOTVALIDATED("player.self.not-validated",
			"Your account must be validated to perform this action."),
	PLAYER_SELF_NOTREGISTERED("player.self.not-registered",
			"You must register for an account to perform this action."),
	PLAYER_SELF_NO_PERMISSION_GENERIC("player.self.no-permission.generic",
			"You don't have permission to perform this action."),
	PLAYER_SELF_NO_PERMISSION_COMMAND("player.self.no-permission.command",
			"You don't have permission to execute this command."),

	COMMAND_NOTAPLAYER("command.not-a-player",
			"You must be a player to perform this command."),

	COMMAND_NOTIFICATIONS_USAGE("command.notifications.usage",
			"{command}"),
	COMMAND_NOTIFICATIONS_DESCRIPTION("command.notifications.description",
			"Displays a list of website notifications"),
	COMMAND_NOTIFICATIONS_OUTPUT_NONOTIFICATIONS("command.notifications.output.no-notifications",
			"You do not have any unread notifications."),
	COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN("command.notifications.output.click-to-open",
			"Click to open in web browser"),
	COMMAND_NOTIFICATIONS_OUTPUT_FAIL_GENERIC("command.notifications.output.fail.generic",
			"An error occured while trying to retrieve a list of notifications. Please notify the server administrator about this issue."),

	COMMAND_REGISTER_USAGE("command.register.usage",
			"{command} <email>"),
	COMMAND_REGISTER_DESCRIPTION("command.register.description",
			"Creates an account. Will output a link or email address to complete registration."),
	COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL("command.register.output.success.email",
			"Please check your inbox to complete registration."),
	COMMAND_REGISTER_OUTPUT_SUCCESS_LINK("command.register.output.success.link",
			"Please visit {link} to complete registration."),
	COMMAND_REGISTER_OUTPUT_FAIL_GENERIC("command.register.output.fail.generic",
			"An error occured while trying to register. Please notify the server administrator about this issue."),
	COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS("command.register.output.fail.already-exists",
			"You already have an account."),
	COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED("command.register.output.fail.email-used",
			"This email address is already used for a different user account."),
	COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID("commands.register.output.fail.email-invalid",
			"The provided email address is invalid."),

	COMMAND_REPORT_USAGE("command.report.usage",
			"{command} <name/uuid> <reason>"),
	COMMAND_REPORT_DESCRIPTION("command.report.description",
			"reports a player"),
	COMMAND_REPORT_OUTPUT_SUCCESS("command.report.output.success",
			"Thank you for reporting this player."),
	COMMAND_REPORT_OUTPUT_FAIL_GENERIC("command.report.output.fail.generic",
			"An error occured while trying to report this player. Please notify the server administrator about this issue."),

	COMMAND_SETGROUP_USAGE("command.set-group.usage",
			"{command} <name/uuid> <group id>"),
	COMMAND_SETGROUP_DESCRIPTION("command.set-group.description",
			"Changes a player's group id."),
	COMMAND_SETGROUP_OUTPUT_SUCCESS("command.set-group.output.success",
			"Changed group id for {player} from {old} to {new}"),
	COMMAND_SETGROUP_OUTPUT_FAIL_NOTNUMERIC("command.set-group.output.fail.not-numeric",
			"The provided group id is not a number."),
	COMMAND_SETGROUP_OUTPUT_FAIL_INVALIDGROUPID("command.set-group.output.fail.invalid-group-id",
			"This group ID is invalid."),
	COMMAND_SETGROUP_OUTPUT_FAIL_GENERIC("command.set-group-output.fail.generic",
			"An unknown error occured while trying to set the player's group."),

	COMMAND_VALIDATE_USAGE("command.validate.usage",
			"{command} <code>"),
	COMMAND_VALIDATE_DESCRIPTION("command.validate.description",
			"Validates the user's website account using the given code."),
	COMMAND_VALIDATE_OUTPUT_SUCCESS("command.validate.output.success",
			"Your account has been validated."),
//	COMMAND_VALIDATE_OUTPUT_FAIL_ALREADYVALIDATED("command.validate.output.fail.already-validated",
//			"Your account has already been validated."),
	COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE("command.validate.output.fail.invalid-code",
			"Your validation code is incorrect. Please check if you copied it correctly and try again."),
	COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC("command.user-info.output.fail.generic",
			"An unknown error occured while trying to submit a validation code."),

	COMMAND_USERINFO_USAGE("command.user-info.usage",
			"{command} [name/uuid]"),
	COMMAND_USERINFO_DESCRIPTION("command.user-info.description",
			"Gets information about a user."),
	COMMAND_USERINFO_OUTPUT_USERNAME("command.user-info.output.username",
			"Username: {username}"),
	COMMAND_USERINFO_OUTPUT_DISPLAYNAME("command.user-info.output.displayname",
			"Display name: {displayname}"),
	COMMAND_USERINFO_OUTPUT_UUID("command.user-info.output.uuid",
			"UUID: {uuid}"),
	COMMAND_USERINFO_OUTPUT_GROUP("command.user-info.output.group",
			"Group: {groupname} (id: {id})"),
	COMMAND_USERINFO_OUTPUT_REGISTERDATE("command.user-info.output.registered-date",
			"Registered on {date}"),
	COMMAND_USERINFO_OUTPUT_VALIDATED("command.user-info.output.validated",
			"Account validated: {validated}"),
	COMMAND_USERINFO_OUTPUT_BANNED("command.user-info.output.banned",
			"Banned: {banned}"),
	COMMAND_USERINFO_OUTPUT_BOOLEAN_TRUE("command.user-info.output.boolean.true",
			"yes"),
	COMMAND_USERINFO_OUTPUT_BOOLEAN_FALSE("command.user-info.output.boolean.false",
			"no"),
	COMMAND_USERINFO_OUTPUT_FAIL_GENERIC("command.user-info.output.fail.generic",
			"An unknown error occured while trying to retrieve player information."),
	
	COMMAND_SUBCOMMANDS_USAGE("commands.subcommands.usage",
			"{command} [subcommand] [arguments..]"),
	COMMAND_SUBCOMMANDS_HELP_PREFIX("commands.submenu.help-prefix",
			"/{command}"),

	JOIN_NOTREGISTERED("join.not-registed",
			"You do not have an account on our website yet. Please register using /register"),

	;

	private String path;
	private String defaultMessage;

	Message(final String path, final String defaultMessage){
		this.path = path;
		this.defaultMessage = defaultMessage;
	}

	public String getMessage() {
		return Chat.convertColors(Config.MESSAGES.getConfig().getString(this.path));
	}

	/**
	 * Uses {@link #getMessage()} then replaces placeholders.
	 * <br><br>
	 * "Visit {link} {number} times"
	 * @param placeholders ["link", "https://example.com", "number", 3]
	 * @return "Visit https://example.com 3 times"
	 */
	public String getMessage(final Object... placeholders) {
		if (placeholders.length % 2 != 0) { // False if length is 1, 3, 5, 6, etc.
			throw new IllegalArgumentException("Placeholder array length must be an even number");
		}

		if (placeholders.length == 0) {
			return this.getMessage();
		}

		final Map<String, String> placeholderMap = new HashMap<>();

		Object key = null;

		for (final Object object : placeholders) {
			if (key == null) {
				// 'placeholder' is a key
				key = object;
			} else {
				// Key has been set previously, 'placeholder' must be a value
				placeholderMap.put(key.toString(), object.toString());
				key = null; // Next 'placeholder' is a key
			}
		}

		String message = this.getMessage();

		for(final Map.Entry<String, String> entry : placeholderMap.entrySet()) {
			message = message.replace("{" + entry.getKey() + "}", entry.getValue());
		}

		return message;
	}

	public void send(final CommandSender sender) {
		sender.sendMessage(this.getMessage());
	}

	public void send(final CommandSender sender, final Object... placeholders) {
		sender.sendMessage(this.getMessage(placeholders));
	}

	public static void generateConfig(final Config config) throws IOException {
		final FileConfiguration fileConfig = config.getConfig();
		for (final Message message : Message.values()) {
			if (!fileConfig.contains(message.path)) {
				fileConfig.set(message.path, message.defaultMessage);
			}
		}
		config.setConfig(fileConfig);
		config.save();
	}

}
