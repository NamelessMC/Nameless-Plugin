package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import xyz.derkades.derkutils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LanguageHandler implements Reloadable {

	public enum Term {

		PLAYER_OTHER_NOTFOUND("player.other.not-found"),
		PLAYER_OTHER_NOTREGISTERED("player.other.not-registered"),
		PLAYER_SELF_NOTREGISTERED("player.self.not-registered"),
		PLAYER_SELF_COMMAND_BANNED("player.self.command-banned"),

		COMMAND_NOTAPLAYER("command.not-a-player"),
		COMMAND_NO_PERMISSION("command.no-permission"),

		COMMAND_NOTIFICATIONS_USAGE("command.notifications.usage"),
		COMMAND_NOTIFICATIONS_DESCRIPTION("command.notifications.description"),
		COMMAND_NOTIFICATIONS_OUTPUT_NO_NOTIFICATIONS("command.notifications.output.no-notifications"),
		COMMAND_NOTIFICATIONS_OUTPUT_NOTIFICATION("command.notifications.output.notification"),
		COMMAND_NOTIFICATIONS_OUTPUT_FAIL("command.notifications.output.fail"),

		COMMAND_PLUGIN_USAGE("command.plugin.usage"),
		COMMAND_PLUGIN_DESCRIPTION("command.plugin.description"),
		COMMAND_PLUGIN_OUTPUT_RELOAD_SUCCESSFUL("command.plugin.output.reload-successful"),

		COMMAND_REGISTER_USAGE("command.register.usage"),
		COMMAND_REGISTER_DESCRIPTION("command.register.description"),
		COMMAND_REGISTER_OUTPUT_SUCCESS_EMAIL("command.register.output.success.email"),
		COMMAND_REGISTER_OUTPUT_SUCCESS_LINK("command.register.output.success.link"),
		COMMAND_REGISTER_OUTPUT_FAIL_GENERIC("command.register.output.fail.generic"),
		COMMAND_REGISTER_OUTPUT_FAIL_USERNAMEUSED("command.register.output.fail.username-used"),
		COMMAND_REGISTER_OUTPUT_FAIL_EMAILUSED("command.register.output.fail.email-used"),
		COMMAND_REGISTER_OUTPUT_FAIL_MINECRAFTUSED("command.register.output.fail.minecraft-used"),
		COMMAND_REGISTER_OUTPUT_FAIL_EMAILINVALID("command.register.output.fail.email-invalid"),
		COMMAND_REGISTER_OUTPUT_FAIL_USERNAMEINVALID("command.register.output.fail.username-invalid"),
		COMMAND_REGISTER_OUTPUT_FAIL_CANNOTSENDEMAIL("command.register.output.fail.cannot-send-email"),

		COMMAND_REPORT_USAGE("command.report.usage"),
		COMMAND_REPORT_DESCRIPTION("command.report.description"),
		COMMAND_REPORT_OUTPUT_SUCCESS("command.report.output.success"),
		COMMAND_REPORT_OUTPUT_FAIL_GENERIC("command.report.output.fail.generic"),
		COMMAND_REPORT_OUTPUT_FAIL_ALREADY_OPEN("command.report.output.fail.already-open"),
		COMMAND_REPORT_OUTPUT_FAIL_REPORT_SELF("command.report.output.fail.report-self"),

		COMMAND_VALIDATE_USAGE("command.validate.usage"),
		COMMAND_VALIDATE_DESCRIPTION("command.validate.description"),
		COMMAND_VALIDATE_OUTPUT_SUCCESS("command.validate.output.success"),
		COMMAND_VALIDATE_OUTPUT_FAIL_INVALIDCODE("command.validate.output.fail.invalid-code"),
		COMMAND_VALIDATE_OUTPUT_FAIL_ALREADYVALIDATED("command.validate.output.fail.already-validated"),
		COMMAND_VALIDATE_OUTPUT_FAIL_GENERIC("command.validate.output.fail.generic"),

		COMMAND_USERINFO_USAGE("command.user-info.usage"),
		COMMAND_USERINFO_DESCRIPTION("command.user-info.description"),
		COMMAND_USERINFO_OUTPUT_USERNAME("command.user-info.output.username"),
		COMMAND_USERINFO_OUTPUT_DISPLAYNAME("command.user-info.output.displayname"),
		COMMAND_USERINFO_OUTPUT_PRIMARY_GROUP("command.user-info.output.primary-group"),
		COMMAND_USERINFO_OUTPUT_ALL_GROUPS("command.user-info.output.all-groups"),
		COMMAND_USERINFO_OUTPUT_REGISTERDATE("command.user-info.output.registered-date"),
		COMMAND_USERINFO_OUTPUT_VALIDATED("command.user-info.output.validated"),
		COMMAND_USERINFO_OUTPUT_BANNED("command.user-info.output.banned"),
		COMMAND_USERINFO_OUTPUT_CUSTOM_FIELD("command.user-info.output.custom-field"),
		COMMAND_USERINFO_OUTPUT_YES("command.user-info.output.yes"),
		COMMAND_USERINFO_OUTPUT_NO("command.user-info.output.no"),
		COMMAND_USERINFO_OUTPUT_FAIL("command.user-info.output.fail"),

		JOIN_NOTREGISTERED("join-not-registered"),
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
	private static final int VERSION = 23;

	private static final Set<String> LANGUAGES = new HashSet<>();
	static {
		LANGUAGES.add("cs_CZ");
		LANGUAGES.add("de_DE");
		LANGUAGES.add("el_GR");
		LANGUAGES.add("en_UK");
		LANGUAGES.add("en_US");
		LANGUAGES.add("es_419");
		LANGUAGES.add("es_ES");
		LANGUAGES.add("fr_FR");
		LANGUAGES.add("he_IL");
		LANGUAGES.add("hr_HR");
		LANGUAGES.add("hu_HU");
		LANGUAGES.add("it_IT");
		LANGUAGES.add("ja_JP");
		LANGUAGES.add("ko_KR");
		LANGUAGES.add("lt_LT");
		LANGUAGES.add("nb_NO");
		LANGUAGES.add("nl_NL_form");
		LANGUAGES.add("nl_NL");
		LANGUAGES.add("pl_PL");
		LANGUAGES.add("pt_BR");
		LANGUAGES.add("ro_RO");
		LANGUAGES.add("ru_RU");
		LANGUAGES.add("sk_SK");
		LANGUAGES.add("sv_SE");
		LANGUAGES.add("tr_TR");
		LANGUAGES.add("vi_VN");
		LANGUAGES.add("zh_CN");
	}

	private static final String DEFAULT_LANGUAGE = "en_UK";
	private static final String VERSION_FILE_NAME = ".VERSION_DO_NOT_DELETE.dat";
	private static final ConfigurationProvider CONFIGURATION_PROVIDER =
			ConfigurationProvider.getProvider(YamlConfiguration.class);

	private Configuration fallbackLanguageFile = null;
	private Configuration activeLanguageFile = null;

	private final @NotNull Path dataDirectory;
	private final @NotNull Path languageDirectory;
	private final @NotNull ConfigurationHandler config;
	private final @NotNull AbstractLogger logger;

	public LanguageHandler(final @NotNull Path dataDirectory,
						   final @NotNull ConfigurationHandler config,
						   final @NotNull AbstractLogger logger) {
		this.dataDirectory = dataDirectory;
		this.languageDirectory = dataDirectory.resolve("languages");
		this.config = config;
		this.logger = logger;
	}

	@Override
	public void reload() {
		final String languageCode = this.config.getMainConfig()
				.getString("language", DEFAULT_LANGUAGE);
		try {
			this.updateFiles();
			this.setActiveLanguage(languageCode);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getRawMessage(final Term term) {
		String message = this.activeLanguageFile.getString(term.path);
		if (message == null) {
			message = this.fallbackLanguageFile.getString(term.path);
		}
		return Objects.requireNonNull(message,
				"Message '" + term.path + "' missing from base language file. This is a bug, please report it.");
	}

	public String getLegacyMessage(final Term term) {
		Component c = getComponent(term);
		return LegacyComponentSerializer.legacySection().serialize(c);
	}

	public String getLegacyMessage(final Term term, final String... placeholders) {
		Component c = getComponent(term, placeholders);
		return LegacyComponentSerializer.legacySection().serialize(c);
	}

	public Component getComponent(final Term term) {
		return MiniMessage.miniMessage().deserialize(getRawMessage(term)); // TODO cache?
	}

	public Component getComponent(final Term term, final String... placeholders) {
		TagResolver[] resolvers = new TagResolver[placeholders.length / 2];
		for (int i = 0; i < placeholders.length; i+=2) {
			resolvers[i / 2] = Placeholder.parsed(placeholders[i], placeholders[i+1]);
		}
		return MiniMessage.miniMessage().deserialize(getRawMessage(term), resolvers);
	}

	private void updateFiles() throws IOException {
		Files.createDirectories(this.languageDirectory);

		final Path versionFile = this.languageDirectory.resolve(VERSION_FILE_NAME);

		if (Files.exists(versionFile)) {
			final String versionContent = Files.readString(versionFile);
			if (versionContent.equals(String.valueOf(VERSION))) {
				this.logger.info("Language files up to date");
				return;
			} else {
				this.logger.warning("Language files are outdated!");
				this.logger.info("Making backup of old languages directory");
				Path dest = this.dataDirectory.resolve("languages-backup-" + System.currentTimeMillis());
				Files.move(this.languageDirectory, dest);
				Files.createDirectory(this.languageDirectory);
			}
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

	private Configuration readLanguageFile(final @NotNull String languageName) throws IOException {
		if (!LANGUAGES.contains(languageName)) {
			this.logger.severe("Language '" + languageName + "' not known.");
			return null;
		}

		final Path file = this.languageDirectory.resolve(languageName + ".yaml");

		try (final InputStream in = Files.newInputStream(file)) {
			return CONFIGURATION_PROVIDER.load(in);
		}
	}

	private void setActiveLanguage(final @NotNull String languageCode) throws IOException {
		this.activeLanguageFile = readLanguageFile(languageCode);
		if (languageCode.equals(DEFAULT_LANGUAGE)) {
			this.fallbackLanguageFile = this.activeLanguageFile;
		} else {
			this.fallbackLanguageFile = readLanguageFile(DEFAULT_LANGUAGE);
		}
	}

}
