package _test.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.List;
import java.util.stream.Stream;

import static _test.providers.Trim.subList_First;
import static _test.providers.Trim.toArgs;

/** Format ( String text ) */
public class SetupProvider {
	
	private static final List<String> Directives_Only=List.of(
			".data", ".text", ".code"
	);
	private static final List<String> DataTypes_Only=List.of(
			".word"
	);
	private static final List<String> Invalid_Directives=List.of(
			".panda", ".global", ".extern", ".macro", ".wo rd", ".WORD"
	);
	private static final List<String> Invalid_DataTypes_Only=List.of(
			".double", ".half", ".byte", ".float"
	);
	private static final List<String> Labels=List.of(
			"_label.with-splits", "_label", "label", "label0"
	);
	private static final List<String> Invalid_Labels_Only=List.of(
			"_ label with spaces", "CAPITALIZED","73label"
	);
	
	public static class ValidDirectives implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return toArgs( Directives_Only, DataTypes_Only );
		}
		
	}
	
	public static class InvalidDirectives implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return toArgs( Invalid_Directives, Invalid_DataTypes_Only );
		}
		
	}
	
	public static class ValidDataTypes implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return toArgs(DataTypes_Only);
		}
		
	}
	
	public static class InvalidDataTypes implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return toArgs(Invalid_Directives, Invalid_DataTypes_Only,
						  subList_First( Directives_Only ), subList_First( Labels ));
		}
		
	}
	
	public static class ValidLabels implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) { return toArgs(Labels); }
		
	}
	
	public static class InvalidLabels implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return toArgs( Invalid_Labels_Only, subList_First( DataTypes_Only ) );
		}
		
	}
	
}
