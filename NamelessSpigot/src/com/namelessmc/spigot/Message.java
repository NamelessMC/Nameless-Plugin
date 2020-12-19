package com.namelessmc.spigot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.io.Files;

public enum Message {

	PLAYER_OTHER_NOTFOUND("player.other.not-found"),
	PLAYER_OTHER_NOTVALIDATED("player.other.not-validated"),
	PLAYER_OTHER_NOTREGISTERED("player.other.not-registered"),
	PLAYER_SELF_NOTVALIDATED("player.self.not-validated"),
	PLAYER_SELF_NOTREGISTERED("player.self.not-registered"),
	PLAYER_SELF_NO_PERMISSION_GENERIC("player.self.no-permission"),

	COMMAND_NOTAPLAYER("command.not-a-player"),
	COMMAND_NO_PERMISSION("command.no-permission"),

	COMMAND_NOTIFICATIONS_USAGE("command.notifications.usage"),
	COMMAND_NOTIFICATIONS_DESCRIPTION("command.notifications.description"),
	COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS("command.notifications.output.no-notifications"),
	COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN("command.notifications.output.click-to-open"),
	COMMAND_NOTIFICATIONS_OUTPUT_FAIL("command.notifications.output.fail"),

	COMMAND_REGISTER_USAGE("command.register.usage"),
	COMMAND_REGISTER_DESCRIPTION("command.register.description"),
	COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL("command.register.output.success.email"),
	COMMAND_REGISTER_OUTPUT_SUCCESS_LINK("command.register.output.success.link"),
	COMMAND_REGISTER_OUTPUT_FAIL_GENERIC("command.register.output.fail.generic"),
	COMMAND_REGISTER_OUTPUT_FAIL_ALREADYEXISTS("command.register.output.fail.already-exists"),
	COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED("command.register.output.fail.email-used"),
	COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID("commands.register.output.fail.email-invalid"),

	COMMAND_REPORT_USAGE("command.report.usage"),
	COMMAND_REPORT_DESCRIPTION("command.report.description"),
	COMMAND_REPORT_OUTPUT_SUCCESS("command.report.output.success"),
	COMMAND_REPORT_OUTPUT_FAIL_GENERIC("command.report.output.fail.generic"),

	COMMAND_VALIDATE_USAGE("command.validate.usage"),
	COMMAND_VALIDATE_DESCRIPTION("command.validate.description"),
	COMMAND_VALIDATE_OUTPUT_SUCCESS("command.validate.output.success"),
	COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE("command.validate.output.fail.invalid-code"),
	COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC("command.user-info.output.fail.generic"),

	COMMAND_USERINFO_USAGE("command.user-info.usage"),
	COMMAND_USERINFO_DESCRIPTION("command.user-info.description"),
	COMMAND_USERINFO_OUTPUT_USERNAME("command.user-info.output.username"),
	COMMAND_USERINFO_OUTPUT_DISPLAYNAME("command.user-info.output.displayname"),
	COMMAND_USERINFO_OUTPUT_UUID("command.user-info.output.uuid"),
	COMMAND_USERINFO_OUTPUT_GROUP("command.user-info.output.group"),
	COMMAND_USERINFO_OUTPUT_REGISTERDATE("command.user-info.output.registered-date"),
	COMMAND_USERINFO_OUTPUT_VALIDATED("command.user-info.output.validated"),
	COMMAND_USERINFO_OUTPUT_BANNED("command.user-info.output.banned"),
	COMMAND_USERINFO_OUTPUT_YES("command.user-info.output.yes"),
	COMMAND_USERINFO_OUTPUT_NO("command.user-info.output.no"),
	COMMAND_USERINFO_OUTPUT_FAIL("command.user-info.output.fail"),
	
	COMMAND_SUBCOMMANDS_USAGE("command.subcommands.usage"),
	COMMAND_SUBCOMMANDS_HELP_PREFIX("command.subcommands.help-prefix"),

	JOIN_NOTREGISTERED("join-not-registed"),

	;
	
	/**
	 * Language version. Increment by one when adding, removing, or changing strings.
	 */
	private static final int VERSION = 1;
	
	private static final String[] LANGUAGES_LIST = {
			"en",
	};

	private static final Charset VERSION_FILE_CHARSET = Charset.forName("UTF-8");
	private static final String VERSION_FILE_NAME = ".VERSION_DO_NOT_DELETE.dat";

	private String path;

	Message(final String path) {
		this.path = path;
	}

	private static FileConfiguration fallbackLanguageFile = null;
	private static FileConfiguration activeLanguageFile = null;

	public String getMessage() {
		String unconverted = activeLanguageFile.getString(this.path);
		if (unconverted == null) {
			unconverted = fallbackLanguageFile.getString(this.path);
		}
		Validate.notNull(unconverted, "Message '" + this.path + "' missing from language file. This is a bug, adding it to the language file is usually not the correct solution.");
		return Chat.convertColors(unconverted);
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
		
		Validate.noNullElements(placeholders);

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
	
	private static File getLanguageDirectory() {
		return new File(NamelessPlugin.getInstance().getDataFolder(), "languages");
	}

	public static void updateFiles() throws IOException {
		final Logger log = NamelessPlugin.getInstance().getLogger();
		
		final File languageDirectory = getLanguageDirectory();
		languageDirectory.mkdirs();
		
		final File versionFile = new File(languageDirectory, VERSION_FILE_NAME);
		
		if (versionFile.exists()) {
			final String versionContent = FileUtils.readFileToString(versionFile, VERSION_FILE_CHARSET);
			if (versionContent.equals(String.valueOf(VERSION))) {
				log.info("Language files up to date");
				return;
			} else {
				log.warning("Language files are outdated!");
				log.info("Making backup of old languages directory");
				final File dest = new File(NamelessPlugin.getInstance().getDataFolder(), "oldlanguages-" + System.currentTimeMillis());
				Files.move(languageDirectory, dest);
				languageDirectory.mkdir();
			}
		} else {
			log.warning("Languages appear to not be installed yet.");
		}
		
		log.info("Installing language files");
		
		for (final String languageName : LANGUAGES_LIST) {
			final String languagePathInJar = "/languages/" + languageName + ".yaml";
			final File dest = new File(languageDirectory, languageName + ".yaml");
			dest.createNewFile();
			FileUtils.copyURLToFile(Message.class.getResource(languagePathInJar), dest);
		}
		
		log.info("Creating version file");
		
		FileUtils.writeStringToFile(versionFile, String.valueOf(VERSION), VERSION_FILE_CHARSET);
		
		log.info("Done");
	}
	
	private static FileConfiguration readLanguageFile(final String languageName) {
		final Logger log = NamelessPlugin.getInstance().getLogger();
		
		if (!ArrayUtils.contains(LANGUAGES_LIST, languageName)) {
			log.severe("Language '" + languageName + "' not known.");
			return null;
		}
		
		final File file = new File(getLanguageDirectory(), languageName + ".yaml");
		
		if (!file.exists()) {
			log.severe("File not found: '" + file.getAbsolutePath() + "'");
			return null;
		}
		
		return YamlConfiguration.loadConfiguration(file);
	}
	
	public static boolean setActiveLanguage(final String languageName) {
		activeLanguageFile = readLanguageFile(languageName);
		if (languageName.equals("en")) {
			fallbackLanguageFile = activeLanguageFile;
		} else {
			fallbackLanguageFile = readLanguageFile("en");
		}
		return activeLanguageFile != null && fallbackLanguageFile != null;
	}

}
