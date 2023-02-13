import com.namelessmc.plugin.common.LanguageHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;

public class LanguageHandlerTest {

	@Test
	void ensureAllFilesInLanguageSet() throws IllegalAccessException, NoSuchFieldException, IOException {
		Field field = LanguageHandler.class.getDeclaredField("LANGUAGES");
		field.setAccessible(true);
		Set<String> languageCodes = (Set<String>) field.get(null);;
		Path languagesDir = Path.of( "resources", "languages");
		Files.list(languagesDir).forEach(path -> {
			String name = path.getFileName().toString();
			String languageCode = name.substring(0, name.length() - 5);
			Assertions.assertTrue(languageCodes.contains(languageCode), "missing language " + languageCode);
		});
	}

	@Test
	void ensureTermsPresentInBase() throws ConfigurateException {
		Path path = Path.of("resources", "languages", "en_UK.yaml");
		ConfigurationNode config = YamlConfigurationLoader.builder().path(path).build().load();
		for (LanguageHandler.Term term : LanguageHandler.Term.values()) {
			Assertions.assertTrue(config.hasChild(term.path()), "base file en_UK missing term " + Arrays.toString(term.path()));
		}
	}

	@Test
	void ensureLanguageFilesPresent() {
		for (String language : LanguageHandler.LANGUAGES) {
			Assertions.assertTrue(Files.isRegularFile(Paths.get("resources", "languages", language + ".yaml")));
		}
	}

}
