package test_util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/** Stream of null, empty, and blank (single/double/quad whitespace, tab, and newline) */
public class NullOrBlankProvider implements ArgumentsProvider {
	
	public Stream provideArguments(ExtensionContext context) {
		return Stream.of( null, "", " ", "  ", "    ", "\t", "\n" ).map( Arguments::of );
	}
	
}
