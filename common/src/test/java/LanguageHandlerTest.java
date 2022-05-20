import com.namelessmc.plugin.common.LanguageHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
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

}
