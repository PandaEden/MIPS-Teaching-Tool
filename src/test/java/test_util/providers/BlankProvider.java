package test_util.providers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/** Stream of null, empty, and blank (single/double/quad whitespace, tab, and newline) */
public class BlankProvider {
	public static class NullNwLn implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
		return Stream.of( null, " ", "\t", "\n" ).map( Arguments::of );
	}
	}
	
	public static class NwLn implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of( " ", "  ", "    ", "\t", "\n" ).map( Arguments::of );
		}
	}
	public static class Extensive implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of( " ", "  ", "    ", "\t" ).map( Arguments::of );
		}
	}
	
	public static class Null implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of( null,  "   " ).map( Arguments::of );
		}
	}
}
