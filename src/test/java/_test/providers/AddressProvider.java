package _test_util.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static _test_util.providers.Trim.first;

/**
 @see model.components.InstrMemory#ADDR_SIZE
 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
 @see model.components.InstrMemory#OVER_SUPPORTED_INSTR_ADDRESS
 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
 @see model.components.DataMemory#DATA_ALIGN
 @see model.components.DataMemory#BASE_DATA_ADDRESS
 @see model.components.DataMemory#OVER_SUPPORTED_DATA_ADDRESS
 @see model.components.DataMemory#OVER_DATA_ADDRESS */
public class AddressProvider {
	// Addresses above 0x8000,0000 become negative signed integers.
	// use an Long to hold these values.
	
	// Steps of 4 :: Hex
	//		0, 4, 8, C
	
	// I could refactor this to use Integer.decode(  )
	// to convert the String form to int.
	private static final List<Arguments> NotValid_Negative=List.of(
			Arguments.of( "0x80000000", Integer.MIN_VALUE ),
			Arguments.of( "0xFFFFFFF8", -8 ),
			Arguments.of( "0xFFFFFFFC", -4 ) );
	private static final List<Arguments> NotValid_Under=List.of(
			Arguments.of( "0x00000000", 0x00000000 ),    // Under Valid Instr
			Arguments.of( "0x00000004", 0x00000004 ),    // +1
			Arguments.of( "0x003FFFF8", 0x003FFFF8 ),    // Max-1
			Arguments.of( "0x003FFFFC", 0x003FFFFC ) );  // Max
	private static final List<Arguments> SupportedInstrAddr=List.of(
			Arguments.of( "0x00400000", 0x00400000 ),    // Base
			Arguments.of( "0x00400004", 0x00400004 ),    // Base+1
			Arguments.of( "0x00480000", 0x00480000 ),    // Mid-Point
			Arguments.of( "0x004FFFFC", 0x004FFFFC ),    // Max-1
			Arguments.of( "0x00500000", 0x00500000 ) );  // Max
	private static final List<Arguments> ValidInstrAddr=List.of(
			Arguments.of( "0x00500004", 0x00500004 ),    // Base
			Arguments.of( "0x00500008", 0x00500008 ),    // Base+1
			Arguments.of( "0x08280000", 0x08280000 ),    // Mid-Point
			Arguments.of( "0x0FFFFFF8", 0x0FFFFFF8 ),    // Max-1
			Arguments.of( "0x0FFFFFFC", 0x0FFFFFFC ) );  // Max
	private static final List<Arguments> NotValid_Between=List.of(
			Arguments.of( "0x10000000", 0x10000000 ),    // Between Valid Instr&Data
			Arguments.of( "0x10000004", 0x10000004 ),    // +1
			Arguments.of( "0x1000FFF8", 0x1000FFF8 ),    // Max-1
			Arguments.of( "0x1000FFFC", 0x1000FFFC ) );  // Max
	// Aligned 8
	private static final List<Arguments> SupportedDataAddr=List.of(
			Arguments.of( "0x10010000", 0x10010000 ),    // Base
			Arguments.of( "0x10010008", 0x10010008 ),    // Base+1
			Arguments.of( "0x10010400", 0x10010400 ),    // Mid-Point
			Arguments.of( "0x100107F0", 0x100107F0 ),    // Max-1
			Arguments.of( "0x100107F8", 0x100107F8 ) );  // Max
	// Aligned 8
	private static final List<Arguments> ValidDataAddr=List.of(
			Arguments.of( "0x10010800", 0x10010800 ),    // Base
			Arguments.of( "0x10010808", 0x10010808 ),    // Base+1
			Arguments.of( "0x10028400", 0x10028400 ),    // Mid-Point
			Arguments.of( "0x1003FFF0", 0x1003FFF0 ),    // Max-1
			Arguments.of( "0x1003FFF8", 0x1003FFF8 ) );  // Max
	private static final List<Arguments> NotValid_Over=List.of(
			Arguments.of( "0x1003FFFC", 0x1003FFFC ),    // Above Valid Data
			Arguments.of( "0x10040000", 0x10040000 ),    // +1
			Arguments.of( "0x7FFFFFF8", 0x7FFFFFF8 ),    // Max-1
			Arguments.of( "0x7FFFFFFC", 0x7FFFFFFC ) );  // Max Integer Addr
	
	private static final List<Arguments> NotAligned=List.of(
			Arguments.of( "0xFFFFFFFF", -1 ),    // Negative
			Arguments.of( "0x00000001", 1 ),    // Positive
			Arguments.of( "0x00480002", 0x00480002 ),    // Instr Midpoint+2
			Arguments.of( "0x10010402", 0x10010402 ),    // Data Midpoint+2
			Arguments.of( "0x7FFFFFFF", Integer.MAX_VALUE ) );// Odd Number
	
	private static final List<Arguments> NotAligned_Data=List.of(
			Arguments.of( "0x10010404", 0x10010404 ) );    // Data Midpoint+4
	
	private static final List<Arguments> NotValid=Stream.of(
			NotValid_Negative, NotValid_Under, NotValid_Between, NotValid_Over )
														.flatMap( Collection::stream ).collect( Collectors.toList( ) );
	
	public static class InstrAddr {
		public static class Supported implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) { return SupportedInstrAddr.stream( );}
			
		}
		
		public static class Valid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) { return ValidInstrAddr.stream( );}
			
		}
		
		public static class NotValid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) {
				List<Arguments> tempList = List.of( first(SupportedDataAddr) ,first(ValidDataAddr));
				return Stream.of( NotValid, NotAligned, tempList).flatMap( Collection::stream );
			}
			
		}
		
	}
	
	public static class DataAddr {
		public static class Supported implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) {return SupportedDataAddr.stream( );}
			
		}
		
		public static class Valid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) {return ValidDataAddr.stream( );}
			
		}
		
		public static class NotValid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments(ExtensionContext context) {
				List<Arguments> tempList = List.of( SupportedInstrAddr.get( 0 ) ,ValidInstrAddr.get( 0 ));
				return Stream.of( NotValid, NotAligned, tempList, NotAligned_Data ).flatMap( Collection::stream );
			}
			
		}
		
	}
	public static class AddrRange implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {return Stream.of(
				Arguments.of("0x80000000", Integer.MIN_VALUE),
				Arguments.of("0x7FFFFFFF", Integer.MAX_VALUE),
				Trim.first( NotAligned ),
				Trim.first( NotValid_Under ),
				Trim.first( SupportedInstrAddr ),
				Trim.first( ValidInstrAddr ),
				Trim.first( NotValid_Between ),
				Trim.first( SupportedDataAddr ),
				Trim.first( ValidDataAddr ),
				Trim.first( NotValid_Over )
				);
		}
		
	}
	public static class Immediate implements ArgumentsProvider {
		public Stream<Arguments> provideArguments(ExtensionContext context) {return Stream.of(
		null);
		}
		}
	
	}
