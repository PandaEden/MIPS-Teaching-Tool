package _test.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.List;
import java.util.stream.Stream;

import static _test.providers.Trim.*;

/** Format ( String hexAddr, Integer address ) ( , Integer index )
 
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
	static final Arguments INT_MIN=Arguments.of( "0x80000000", Integer.MIN_VALUE );
	static final Arguments INT_MAX=Arguments.of( "0x7FFFFFFF", Integer.MAX_VALUE );
	
	private static final List<Arguments> NotValid_Negative=List.of(
			INT_MIN,
			Arguments.of( "0xFFFF8000",  -32768),
			Arguments.of( "0xFFFFFFF8", -8 ),
			Arguments.of( "0xFFFFFFFC", -4 ) );
	private static final List<Arguments> NotValid_Under=List.of(
			Arguments.of( "0x00000000", 0 ),    // Under Valid Instr
			Arguments.of( "0x00000004", 4 ),    // +1
			Arguments.of( "0x003FFFF8", 4194296 ),    // Max-1
			Arguments.of( "0x003FFFFC", 4194300 ) );  // Max
	private static final List<Arguments> SupportedInstrAddr=List.of(
			Arguments.of( "0x00400000", 4194304, 0),    // Base
			Arguments.of( "0x00400004", 4194308, 1),    // Base+1
			Arguments.of( "0x00400200", 4194816, 128),    // Actual Mid-Point
			Arguments.of( "0x004003F8", 4195320, 254),    // Actual Max-1
			Arguments.of( "0x004003FC", 4195324, 255),    // Actual Max
			Arguments.of( "0x00480000", 4718592, 131072),    // Supported Mid-Point
			Arguments.of( "0x004FFFFC", 5242876, 262143),    // Supported Max-1
			Arguments.of( "0x00500000", 5242880, 262144) );  // Supported Max
	private static final List<Arguments> ValidInstrAddr=List.of(
			Arguments.of( "0x00500004", 5242884 ),    // Base
			Arguments.of( "0x00500008", 5242888 ),    // Base+1
			Arguments.of( "0x08280000", 136839168 ),    // Mid-Point
			Arguments.of( "0x0FFFFFF8", 268435448 ),    // Max-1
			Arguments.of( "0x0FFFFFFC", 268435452 ) );  // Max
	private static final List<Arguments> NotValid_Between=List.of(
			Arguments.of( "0x10000000", 268435456 ),    // Between Valid Instr&Data
			Arguments.of( "0x10000004", 268435460 ),    // +1
			Arguments.of( "0x1000FFF8", 268500984 ),    // Max-1
			Arguments.of( "0x1000FFFC", 268500988 ) );  // Max
	// Aligned 8
	private static final List<Arguments> SupportedDataAddr=List.of(
			Arguments.of( "0x10010000", 268500992, 0),    // Base
			Arguments.of( "0x10010008", 268501000, 2),    // Base+1 [8]
			Arguments.of( "0x10010400", 268502016, 256),    // Mid-Point
			Arguments.of( "0x100107F0", 268503024, 508),    // Max-1 [8]
			Arguments.of( "0x100107F8", 268503032, 510) );  // Max
	// Aligned 8
	private static final List<Arguments> ValidDataAddr=List.of(
			Arguments.of( "0x10010800", 268503040 ),    // Base
			Arguments.of( "0x10010808", 268503048 ),    // Base+1 [8]
			Arguments.of( "0x10028400", 268600320 ),    // Mid-Point
			Arguments.of( "0x1003FFF0", 268697584 ),    // Max-1 [8]
			Arguments.of( "0x1003FFF8", 268697592 ) );  // Max
	private static final List<Arguments> NotValid_Over=List.of(
			Arguments.of( "0x1003FFFC", 268697596 ),    // Above Valid Data
			Arguments.of( "0x10040000", 268697600 ),    // +1
			Arguments.of( "0x7FFFFFF8", 2147483640 ),    // Max-1
			Arguments.of( "0x7FFFFFFC", 2147483644 ) );  // Max Integer Addr
	
	private static final List<Arguments> NotAligned=List.of(
			Arguments.of( "0xFFFFFFFF", -1 ),    // Negative
			Arguments.of( "0x00000001", 1 ),    // Positive
			Arguments.of( "0x00480002", 0x00480002 ),    // Instr Midpoint+2
			Arguments.of( "0x10010402", 0x10010402 ),    // Data Midpoint+2
			INT_MAX );// Odd Number
	
	private static final List<Arguments> NotAligned_Data=List.of(
			Arguments.of( "0x10010404", 0x10010404 ) );    // Data Midpoint+4
	
	private static final List<Arguments> NotValid=combine(
			NotValid_Negative, NotValid_Under, NotValid_Between, NotValid_Over );
	
	public static class InstrAddr {
		public static class Supported implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return SupportedInstrAddr.stream( );}
			
		}
		
		public static class Valid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return ValidInstrAddr.stream( );}
			
		}
		
		public static class NotValid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) {
				return flatMap( NotValid, NotAligned, List.of( first( SupportedDataAddr ), first( ValidDataAddr ) ) );
			}
			
		}
		
	}
	
	public static class DataAddr {
		public static class Supported implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) {return SupportedDataAddr.stream( );}
			
		}
		
		public static class Valid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) {return ValidDataAddr.stream( );}
			
		}
		
		public static class NotValid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) {
				return flatMap( NotValid, NotAligned, NotAligned_Data,
								List.of( first( SupportedInstrAddr ), first( ValidInstrAddr ) ) );
			}
			
		}
		
	}
	
	public static class AddrRange implements ArgumentsProvider {
		public Stream<Arguments> provideArguments (ExtensionContext context) {
			return Stream.of(
					Arguments.of( "0x80000000", Integer.MIN_VALUE ),
					Arguments.of( "0x7FFFFFFF", Integer.MAX_VALUE ),
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
		
		public static class Negative implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) {
				return flatMap(NotValid_Negative);
			}
		}
	}
	
}
