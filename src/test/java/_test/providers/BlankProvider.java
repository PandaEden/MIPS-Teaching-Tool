package _test.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/** Stream of null, empty, and blank (single/double/quad whitespace, tab, and newline) */
public class BlankProvider implements ArgumentsProvider{
	public Stream<Arguments> provideArguments(ExtensionContext context) {
		return Stream.of( " " ).map( Arguments::of );
	}
	
	/** null, " ", \t, \n */
	public static class NullNwLn implements ArgumentsProvider {
		
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of( null, " ", "\t", "\n" ).map( Arguments::of );
		}
		
	}
	
	/** " ", \t, \n */
	public static class NwLn implements ArgumentsProvider {
		
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of( " ", "\t", "\n" ).map( Arguments::of );
		}
		
	}
	
	/** "null",  "   " */
	public static class Null implements ArgumentsProvider {
		
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of( "null", "   " ).map( Arguments::of );
		}
		
	}
	
}
