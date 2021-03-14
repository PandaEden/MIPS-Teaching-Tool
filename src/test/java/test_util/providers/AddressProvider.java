package test_util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class AddressProvider {
	// Addresses above 0x8000,0000 become negative signed integers.
	// use an Long to hold these values.
	
	// Steps of 4 :: Hex
	//		0, 4, 8, C
	
	// I could refactor this to use Integer.decode(  )
	// to convert the String form to int.
	
	//TODO - search "Change when Data becomes word aligned"
	
	public static class supportedInstrAddrProvider implements ArgumentsProvider {
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_SUPPORTED_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					Arguments.of( "0x00400000", 0x00400000 ),// Base
					Arguments.of( "0x00400004", 0x00400004 ),// Base+1
					Arguments.of( "0x00480000", 0x00480000 ),// Mid-Point
					Arguments.of( "0x004FFFFC", 0x004FFFFC ),// Max-1
					Arguments.of( "0x00500000", 0x00500000 ) // Max
			);
		}
		
	}
	public static class validInstrAddrProvider implements ArgumentsProvider{
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					Arguments.of("0x00500004", 0x00500004),// Base
					Arguments.of("0x00500008", 0x00400008),// Base+1
					Arguments.of("0x08280000", 0x08280000),// Mid-Point
					Arguments.of("0x0FFFFFF8", 0x0FFFFFF8),// Max-1
					Arguments.of("0x0FFFFFFC", 0x0FFFFFFC) // Max
			);
		}
	}
	
	public static class supportedDataAddrProvider implements ArgumentsProvider{
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					// Change when Data becomes word aligned
					Arguments.of("0x10010000", 0x10010000),// Base
					Arguments.of("0x10010008", 0x00400004),// Base+1
					Arguments.of("0x10010400", 0x10010400),// Mid-Point
					Arguments.of("0x100107F0", 0x100107F0),// Max-1
					Arguments.of("0x100107F8", 0x100107F8) // Max
			);
		}
	}
	
	public static class validDataAddrProvider implements ArgumentsProvider{
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					// Change when Data becomes word aligned
					Arguments.of("0x10010800", 0x10010800),// Base
					Arguments.of("0x10010808", 0x10010808),// Base+1
					Arguments.of("0x10028400", 0x10028400),// Mid-Point
					Arguments.of("0x1003FFF0", 0x1003FFF0),// Max-1
					Arguments.of("0x1003FFF8", 0x1003FFF8) // Max
			);
		}
	}
	
	public static class inValidAddrProvider implements ArgumentsProvider{
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					Arguments.of("0x00000000", 0x00000000),// Under Valid Instr
					Arguments.of("0x00000004", 0x00000004),// +1
					Arguments.of("0x003FFFF8", 0x003FFFF8),// Max-1
					Arguments.of("0x003FFFFC", 0x003FFFFC),// Max
					
					Arguments.of("0x10000000", 0x10000000),// Between Valid Instr&Data
					Arguments.of("0x10000004", 0x10000004),// +1
					Arguments.of("0x1000FFF8", 0x1000FFF8),// Max-1
					Arguments.of("0x1000FFFC", 0x1000FFFC),// Max
					
					// Change when Data becomes word aligned
					Arguments.of("0x1003FFFC", 0x1003FFFC),// Above Valid Data
					Arguments.of("0x10040000", 0x10040000),// +1
					Arguments.of("0x7FFFFFF8", 0x7FFFFFF8),// Max-1
					Arguments.of("0x7FFFFFFC", 0x7FFFFFFC) // Max Integer Addr
			);
		}
	}
	
	public static class inValidNegativeAddrProvider implements ArgumentsProvider{
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					Arguments.of("0xFFFFFFFC", -4),
					Arguments.of("0xFFFFFFF8", -8),
					Arguments.of("0x80000000", Integer.MIN_VALUE)
			);
		}
	}
	
	public static class notAlignedAddrProvider implements ArgumentsProvider{
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					Arguments.of("0xFFFFFFFF", -1),	// Negative
					Arguments.of("0x00000008", 1),	// Positive
					
					Arguments.of("0x00480002", 0x00480002),	// Instr Midpoint+2
					Arguments.of("0x10010402", 0x10010402),	// Data Midpoint+2
					
					Arguments.of("0x7FFFFFFF", Integer.MAX_VALUE)
			);
		}
	}
	
	public static class notAlignedDataAddrProvider implements ArgumentsProvider{
		/**
		 @return <String,int>
		 @see model.components.InstrMemory#BASE_INSTR_ADDRESS
		 @see model.components.InstrMemory#OVER_INSTR_ADDRESS
		 */
		public Stream<Arguments> provideArguments(ExtensionContext context) {
			return Stream.of(
					// Change when Data becomes word aligned
					Arguments.of("0x10010404", 0x10010404)	// Data Midpoint+4
			);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
