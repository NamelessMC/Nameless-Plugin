package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.derkades.derkutils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public class LanguageHandler implements Reloadable {

	public enum Term {

		ERROR_WEBSITE_USERNAME_NOT_EXIST("error.website-username-not-exist"),
		ERROR_WEBSITE_CONNECTION("error.website-connection"),
		ERROR_USERNAME_NOT_ONLINE("error.username-not-online"),

		PLAYER_SELF_NOT_REGISTERED("player.self.not-registered"),
		PLAYER_SELF_COMMAND_BANNED("player.self.command-banned"),

		BOOLEAN_YES_POSITIVE("boolean.yes-positive"),
		BOOLEAN_YES_NEGATIVE("boolean.yes-negative"),
		BOOLEAN_NO_POSITIVE("boolean.no-positive"),
		BOOLEAN_NO_NEGATIVE("boolean.no-negative"),

		COMMAND_NOT_A_PLAYER("command.not-a-player"),
		COMMAND_NO_PERMISSION("command.no-permission"),

		COMMAND_NOTIFICATIONS_USAGE("command.notifications.usage"),
		COMMAND_NOTIFICATIONS_DESCRIPTION("command.notifications.description"),
		COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS("command.notifications.output.no-notifications"),
		COMMAND_NOTIFICATIONS_OUTPUT_NOTIFICATION("command.notifications.output.notification"),

		COMMAND_PLUGIN_USAGE("command.plugin.usage"),
		COMMAND_PLUGIN_DESCRIPTION("command.plugin.description"),
		COMMAND_PLUGIN_OUTPUT_RELOAD_SUCCESSFUL("command.plugin.output.reload-successful"),

		COMMAND_REGISTER_USAGE("command.register.usage"),
		COMMAND_REGISTER_DESCRIPTION("command.register.description"),
		COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL("command.register.output.success.email"),
		COMMAND_REGISTER_OUTPUT_SUCCESS_LINK("command.register.output.success.link"),
		COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_USED("command.register.output.fail.username-used"),
		COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_USED("command.register.output.fail.email-used"),
		COMMAND_REGISTER_OUTPUT_FAIL_MINECRAFT_USED("command.register.output.fail.minecraft-used"),
		COMMAND_REGISTER_OUTPUT_FAIL_EMAIL_INVALID("command.register.output.fail.email-invalid"),
		COMMAND_REGISTER_OUTPUT_FAIL_USERNAME_INVALID("command.register.output.fail.username-invalid"),
		COMMAND_REGISTER_OUTPUT_FAIL_CANNOT_SEND_EMAIL("command.register.output.fail.cannot-send-email"),

		COMMAND_REPORT_USAGE("command.report.usage"),
		COMMAND_REPORT_DESCRIPTION("command.report.description"),
		COMMAND_REPORT_OUTPUT_SUCCESS("command.report.output.success"),
		COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN("command.report.output.fail.already-open"),
		COMMAND_REPORT_OUTPUT_FAIL_REPORT_SELF("command.report.output.fail.report-self"),

		COMMAND_VALIDATE_USAGE("command.validate.usage"),
		COMMAND_VALIDATE_DESCRIPTION("command.validate.description"),
		COMMAND_VALIDATE_OUTPUT_SUCCESS("command.validate.output.success"),
		COMMAND_VALIDATE_OUTPUT_FAIL_INVALID_CODE("command.validate.output.fail.invalid-code"),
		COMMAND_VALIDATE_OUTPUT_FAIL_ALREADY_VALIDATED("command.validate.output.fail.already-validated"),

		COMMAND_USERINFO_USAGE("command.user-info.usage"),
		COMMAND_USERINFO_DESCRIPTION("command.user-info.description"),
		COMMAND_USERINFO_OUTPUT_USERNAME("command.user-info.output.username"),
		COMMAND_USERINFO_OUTPUT_DISPLAY_NAME("command.user-info.output.displayname"),
		COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP("command.user-info.output.primary-group"),
		COMMAND_USERINFO_OUTPUT_ALL_GROUPS("command.user-info.output.all-groups"),
		COMMAND_USERINFO_OUTPUT_REGISTER_DATE("command.user-info.output.registered-date"),
		COMMAND_USERINFO_OUTPUT_VALIDATED("command.user-info.output.validated"),
		COMMAND_USERINFO_OUTPUT_BANNED("command.user-info.output.banned"),
		COMMAND_USERINFO_OUTPUT_CUSTOM_FIELD("command.user-info.output.custom-field"),
		COMMAND_USERINFO_OUTPUT_INTEGRATIONS_HEADER("command.user-info.output.integrations.header"),
		COMMAND_USERINFO_OUTPUT_INTEGRATIONS_IDENTIFIER("command.user-info.output.integrations.identifier"),
		COMMAND_USERINFO_OUTPUT_INTEGRATIONS_USERNAME("command.user-info.output.integrations.username"),
		COMMAND_USERINFO_OUTPUT_INTEGRATIONS_LINKED_DATE("command.user-info.output.integrations.linked-date"),
		COMMAND_USERINFO_OUTPUT_INTEGRATIONS_VERIFIED("command.user-info.output.integrations.verified"),

		JOIN_NOT_REGISTERED("join-not-registered"),
		JOIN_NOTIFICATIONS("join-notifications"),
		WEBSITE_ANNOUNCEMENT("website-announcement"),
		USER_SYNC_KICK("user-sync-kick"),

		;

		private final String path;

		Term(final String path) {
			this.path = path;
		}

	}

	/**
	 * Language version. Increment by one when adding, removing, or changing strings.
	 */
	private static final int VERSION = 26;

	private static final Set<String> LANGUAGES = Set.of(
			"cs_CZ",
			"de_DE",
			"el_GR",
			"en_UK",
			"en_US",
			"es_419",
			"es_ES",
			"fr_FR",
			"he_IL",
			"hr_HR",
			"hu_HU",
			"it_IT",
			"ja_JP",
			"ko_KR",
			"lt_LT",
			"nb_NO",
			"nl_NL_form",
			"nl_NL",
			"pl_PL",
			"pt_BR",
			"ro_RO",
			"ru_RU",
			"sk_SK",
			"sq_AL",
			"sv_SE",
			"tr_TR",
			"vi_VN",
			"zh_CN"
	);

	private static final String DEFAULT_LANGUAGE = "en_UK";
	private static final String VERSION_FILE_NAME = ".VERSION_DO_NOT_DELETE.dat";
	private static final ConfigurationProvider CONFIGURATION_PROVIDER =
			ConfigurationProvider.getProvider(YamlConfiguration.class);

	private String activeLanguageCode;
	private Configuration fallbackLanguageFile;
	private Configuration activeLanguageFile;

	private final @NonNull Path dataDirectory;
	private final @NonNull Path languageDirectory;
	private final @NonNull ConfigurationHandler config;
	private final @NonNull AbstractLogger logger;

	public LanguageHandler(final @NonNull Path dataDirectory,
						   final @NonNull ConfigurationHandler config,
						   final @NonNull AbstractLogger logger) {
		this.dataDirectory = dataDirectory;
		this.languageDirectory = dataDirectory.resolve("languages");
		this.config = config;
		this.logger = logger;
	}

	public String getActiveLanguageCode() {
		return this.activeLanguageCode;
	}

	@Override
	public void reload() {
		try {
			this.updateFiles();
			this.setActiveLanguage(this.config.main()
					.getString("language", DEFAULT_LANGUAGE));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String raw(final Term term) {
		String message = this.activeLanguageFile.getString(term.path, null);
		if (message == null) {
			this.logger.warning("Message '" + term.path + "' missing from language file, using EnglishUK as fallback. Please help translate: https://translate.namelessmc.com");
			message = this.fallbackLanguageFile.getString(term.path, null);
		}
		return Objects.requireNonNull(message,
				"Message '" + term.path + "' missing from base language file. This is a bug, please report it.");
	}

	public Component get(final Term term) {
		return MiniMessage.miniMessage().deserialize(raw(term)); // TODO cache?
	}

	public Component get(final Term term, final String... placeholders) {
		TagResolver[] resolvers = new TagResolver[placeholders.length / 2];
		for (int i = 0; i < placeholders.length; i+=2) {
			resolvers[i / 2] = Placeholder.parsed(placeholders[i], placeholders[i+1]);
		}
		return MiniMessage.miniMessage().deserialize(raw(term), resolvers);
	}

	public Component get(final Term term, TagResolver... resolvers) {
		return MiniMessage.miniMessage().deserialize(raw(term), resolvers);
	}

	public Component booleanText(final boolean isYes, final boolean yesIsPositive) {
		if (isYes) {
			if (yesIsPositive) {
				return get(Term.BOOLEAN_YES_POSITIVE);
			} else {
				return get(Term.BOOLEAN_YES_NEGATIVE);
			}
		} else {
			if (yesIsPositive) {
				return get(Term.BOOLEAN_NO_NEGATIVE);
			} else {
				return get(Term.BOOLEAN_NO_POSITIVE);
			}
		}
	}

	private void updateFiles() throws IOException {
		Files.createDirectories(this.languageDirectory);

		final Path versionFile = this.languageDirectory.resolve(VERSION_FILE_NAME);

		if (Files.exists(versionFile)) {
			final String versionContent = Files.readString(versionFile);
			if (versionContent.equals(String.valueOf(VERSION))) {
				return;
			}

			this.logger.warning("Language files are outdated!");
			this.logger.info("Making backup of old languages directory");
			Path dest = this.dataDirectory.resolve("languages-backup-" + System.currentTimeMillis());
			Files.move(this.languageDirectory, dest);
			Files.createDirectory(this.languageDirectory);
		} else {
			this.logger.info("Languages appear to not be installed yet.");
		}

		this.logger.info("Installing language files");

		for (final String languageName : LANGUAGES) {
			final String languagePathInJar = "languages/" + languageName + ".yaml";
			final Path dest = this.languageDirectory.resolve(languageName + ".yaml");
			FileUtils.copyOutOfJar(LanguageHandler.class, languagePathInJar, dest);
		}

		this.logger.info("Creating version file");

		final byte[] bytes = String.valueOf(VERSION).getBytes(StandardCharsets.UTF_8);
		Files.write(versionFile, bytes);

		this.logger.info("Done");
	}

	private @NonNull Configuration readLanguageFile(final @NonNull String languageName) throws IOException {
		final Path file = this.languageDirectory.resolve(languageName + ".yaml");

		try (final InputStream in = Files.newInputStream(file)) {
			return CONFIGURATION_PROVIDER.load(in);
		}
	}

	private void setActiveLanguage(final @NonNull String languageCode) throws IOException {
		if (!LANGUAGES.contains(languageCode)) {
			this.logger.severe("Language '" + languageCode + "' not known, using default language.");
			setActiveLanguage(DEFAULT_LANGUAGE);
			return;
		}

		this.activeLanguageCode = languageCode;
		this.activeLanguageFile = readLanguageFile(languageCode);
		if (languageCode.equals(DEFAULT_LANGUAGE)) {
			this.fallbackLanguageFile = this.activeLanguageFile;
		} else {
			this.fallbackLanguageFile = readLanguageFile(DEFAULT_LANGUAGE);
		}
	}

}
