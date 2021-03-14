package test_util.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Non_OperandsProvider {
	private static final List<String> Directives_Only=List.of(
			".data", ".text", ".code"
	);
	private static final List<String> DataTypes_Only=List.of(
			".word"
	);
	private static final List<String> Invalid_Directives=List.of(
			".panda",".global", ".extern", ".macro", ".wo rd", ".WORD"
	);
	private static final List<String> Invalid_DataTypes_Only=List.of(
			".double", ".half", ".byte", ".float"
	);
	private static final List<String> Labels=List.of(
			"_label.with-splits", "_label", "label", "label0"
	);
	private static final List<String> Invalid_Labels_Only=List.of(
			"_ label with spaces", "73label", "CAPITALIZED"
	);
	private static List<String> first(List<String> list){return list.subList( 0,1 );}
	
	public static class ValidDirectives implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Directives_Only, DataTypes_Only)
						 .flatMap( Collection::stream ).map( Arguments::of );}
	}
	
	public static class InvalidDirectives implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Invalid_Directives, Invalid_DataTypes_Only)
						 .flatMap( Collection::stream ).map( Arguments::of );}
	}
	
	public static class ValidDataTypes implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return DataTypes_Only.stream().map( Arguments::of ); }
	}
	
	public static class InvalidDataTypes implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Invalid_Directives, Invalid_DataTypes_Only, first(Directives_Only), first(Labels))
						 .flatMap( Collection::stream ).map( Arguments::of );}
	}
	
	public static class ValidLabels implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Labels.stream().map( Arguments::of ); }
	}
	public static class InvalidLabels implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(Invalid_Labels_Only, first(DataTypes_Only))
						 .flatMap( Collection::stream ).map( Arguments::of );}
	}
}
