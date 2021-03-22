package _test.providers;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.List;
import java.util.stream.Stream;

import static _test.providers.Trim.*;

/** Format ( String hexAddr, Integer address, String hexImm, Integer imm ) */
public class ImmediateProvider {
	
	//					  Hex_Addr		Int_Addr	Hex_Imm		  Int_Imm
	private static final List<Arguments> _16bit=List.of(
			Arguments.of( "0xFFFE0000", -131072, "0xFFFF8000", -32768 ),    // -2^15
			Arguments.of( "0x0001FFFC", 131068, "0x00007FFF", 32767 ) );    // 2^15-1
	private static final List<Arguments> Invalid_16bit=List.of(
			Arguments.of( "0xFFFDFFFC", -131076, "0xFFFF7FFF", -32769 ),    // (-2^15) -1
			Arguments.of( "0x00020000", 131072, "0x00008000", 32768 ) );    // (2^15-1) +1
	
	private static final List<Arguments> u_26bit=List.of(
			Arguments.of( "0x00000000", 0, "0x00000000", 0 ),         // 0
			Arguments.of( "0x0FFFFFFC", 268435452, "0x03FFFFFF", 67108863 ) ); // 2^26-1
	private static final List<Arguments> Invalid_26bit=List.of(
			Arguments.of( "0xFFFFFFFC", -4, "0xFFFFFFFF", -1 ),         // (0) -1
			Arguments.of( "0x10000000", 268435456, "0x04000000", 67108864 ) );  // 2^26
	
	private static final List<Arguments> Instr_Imm=List.of(
			Arguments.of( "0x00400000", 4194304, "0x00100000", 1048576 ),    // Base_Instr
			Arguments.of( "0x00500000", 5242880, "0x00140000", 1310720 ) );   // Max_Instr
	private static final List<Arguments> Invalid_Instr_Imm=List.of(
			Arguments.of( "0x003FFFFC", 4194300, "0x000FFFFF", 1048575 ),    // Base_Instr -1
			Arguments.of( "0x00500004", 5242884, "0x00140001", 1310721 ) );   // Max_Instr +1
	
	public static class _16Bit implements ArgumentsProvider {
		public Stream<Arguments> provideArguments (ExtensionContext context) { return _16bit.stream( ); }
		
		public static class Invalid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return Invalid_16bit.stream( ); }
			
		}
		
	}
	
	public static class u_26Bit implements ArgumentsProvider {
		public Stream<Arguments> provideArguments (ExtensionContext context) { return u_26bit.stream( ); }
		
		public static class Invalid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return Invalid_26bit.stream( ); }
			
		}
		
	}
	
	public static class Instr_Imm implements ArgumentsProvider {
		public Stream<Arguments> provideArguments (ExtensionContext context) { return Instr_Imm.stream( ); }
		
		public static class Invalid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return Invalid_Instr_Imm.stream( ); }
			
		}
		
	}
	
	public static class All implements ArgumentsProvider {
		public Stream<Arguments> provideArguments (ExtensionContext context) {
			return flatMap( _16bit, u_26bit, Instr_Imm );
		}
		public static class IncInvalid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) {
				return flatMap( _16bit, Invalid_16bit, u_26bit, Invalid_26bit, Instr_Imm, Invalid_Instr_Imm );
			}
			
		}
		
	}
	
	/** Format ( String hexImm, Integer imm ) */
	public static class ConvertInvalid {
		private static final List<Arguments> OutOfRange=List.of(
				Arguments.of( "0xFFFF7FFF", -32769 ),    // (-2^15) -1
				Arguments.of( "0x1FFFFFFF", 536870911 ),   // Integer.MAX_VALUE/4)-1
				AddressProvider.INT_MIN, AddressProvider.INT_MAX );
		
		public static class OutOfRange implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return OutOfRange.stream( ); }
			
		}
		
		public static class Boundary implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) {
				return flatMap(OutOfRange.subList( 0,2 ));
			}
			
		}
		
	}
	
	private static final List<Arguments> _32Bit_Positive= List.of(
			AddressProvider.ZERO, // 0
			Arguments.of( "0x3FFFFFFF", 1073741823 ), // Midpoint (2^30-1)
			AddressProvider.INT_MAX ); // Max (2^31-1)
	private static final List<Arguments> _32Bit_Negative= List.of(
			AddressProvider.MINUS_1,
			Arguments.of( "0xC0000000", -1073741824 ), // Midpoint -(2^30)
			AddressProvider.INT_MIN ); // Max -(2^31)
	private static final List<Arguments> Invalid_32Bit = List.of(
			Arguments.of( "0xFFFFFFFF7FFFFFFF", -2147483649L ),	// (Int.Min)-1
			Arguments.of( "0x0000000080000000", 2147483648L ) ); // (Int.Max)+1
	
	/** Format ( String hexImm, Integer imm ) */
	public static class _32Bit implements ArgumentsProvider{
		public Stream<Arguments> provideArguments (ExtensionContext context) {
			return flatMap( _32Bit_Positive, _32Bit_Negative );
		}
		
		public static class Positive implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return _32Bit_Positive.stream( ); }
			public static class Invalid_Over implements ArgumentsProvider {
				public Stream<Arguments> provideArguments (ExtensionContext context) { return Invalid_32Bit.subList( 1 , 2).stream( ); }
				
			}
			
		}
		public static class Negative implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return _32Bit_Negative.stream( ); }
			
		}
		public static class Invalid implements ArgumentsProvider {
			public Stream<Arguments> provideArguments (ExtensionContext context) { return Invalid_32Bit.stream( ); }
			
		}
	}
}
