package util;

import _test.Tags;
import _test.providers.AddressProvider;
import _test.providers.ImmediateProvider;
import _test.providers.RegisterProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag ( Tags.Pkg.UTIL )
@Tag ( "Convert" )
@DisplayName ( Tags.Pkg.UTIL + " : Convert Test" )
class ConvertTest {
	@Test
	void SplitCSV ( ) {
		String csv="    Lots Of    Empty,    Spaces    , csv should     trim  ";
		String[] result_csv=Convert.splitCSV( csv );
		assertEquals( 3, result_csv.length );
		Assertions.assertAll(
				( ) -> assertEquals( "Lots Of Empty", result_csv[ 0 ] ),
				( ) -> assertEquals( "Spaces", result_csv[ 1 ] ),
				( ) -> assertEquals( "csv should trim", result_csv[ 2 ] )
		);
		
		String s="    one  word ";
		String[] result=Convert.splitCSV( s );
		assertEquals( 1, result.length );
		assertEquals( "one word", result[ 0 ] );
	}
	
	@Test
	void SplitCSV_WithTrailingEmptySegments ( ) {
		String[] result_csv=Convert.splitCSV( "val1, ," );
		assertEquals( 3, result_csv.length );
		Assertions.assertAll(
				( ) -> assertEquals( "val1", result_csv[ 0 ] ),
				( ) -> assertEquals( "", result_csv[ 1 ] ),
				( ) -> assertEquals( "", result_csv[ 2 ] )
		);
	}
	
	@Nested
	@Tag ( Tags.INT )
	@DisplayName ( "Hex <-> Integer" )
	class Hex_Int {
		
		@ParameterizedTest ( name="[{index}] - Convert Int: \"{1}\" To Hex" )
		@ArgumentsSource ( AddressProvider.AddrRange.class )
		@ArgumentsSource ( ImmediateProvider.All.IncInvalid.class )
		void testIntegerToHexadecimal (String hex, Integer address) {
			assertEquals( hex, Convert.int2Hex( address ) );
		}
		
		@ParameterizedTest ( name="[{index}] - Convert hex: \"{0}\" To Int" )
		@ArgumentsSource ( AddressProvider.AddrRange.class )
		@ArgumentsSource ( ImmediateProvider.All.IncInvalid.class )
		void testHexadecimalToInteger (String hex, Integer address) {
			assertEquals( address, Convert.hex2uInt( hex ) );
		}
		
		@ParameterizedTest ( name="[{index}] Fail Convert Invalid Int: \"{0}\" To Hex" )
		@NullSource
		void testInvalid_IntegerToHexadecimal (Integer integer) {
			assertThrows( NullPointerException.class, ( ) -> Convert.int2Hex( integer ) );
		}
		
		@ParameterizedTest ( name="[{index}] Fail Convert Invalid Hex: \"{0}\" To Int" )
		@ValueSource ( strings={ "string", "0x 00001010", "0x", "0xpanda", "572" } )
		void testInvalid_HexadecimalToInteger (String hex) {
			assertThrows( IllegalArgumentException.class, ( ) -> Convert.hex2uInt( hex ) );
		}
		
	}
	
	@Nested
	@Tag ( Tags.ADDR )
	@DisplayName ( "Address <-> Immediate" )
	class Address_Immediate {
		
		@ParameterizedTest ( name="[{index}] - Convert Address: \"[{0}, {1}]\" To Immediate" )
		@ArgumentsSource ( ImmediateProvider.Instr_Imm.class )
		@ArgumentsSource ( ImmediateProvider.u_26Bit.class )
		void testAddressToImmediate (String addr, Integer address, String hex, Integer imm) {
			assertEquals( imm, Convert.address2Imm( address ) );
			assertEquals( hex, Convert.int2Hex( Convert.address2Imm( Convert.hex2uInt( addr ) ) ) );
			
		}
		
		@ParameterizedTest ( name="[{index}] - Convert Address: \"[{2}, {3}]\" To Immediate" )
		@ArgumentsSource ( ImmediateProvider.Instr_Imm.class )
		@ArgumentsSource ( ImmediateProvider.u_26Bit.class )
		void testImmediateToAddress (String addr, Integer address, String hex, Integer imm) {
			assertEquals( address, Convert.imm2Address( imm ) );
			assertEquals( addr, Convert.int2Hex( Convert.imm2Address( Convert.hex2uInt( hex ) ) ) );
			
		}
		
		@ParameterizedTest ( name="[{index}] - Fail Convert Invalid Address: \"{1}\" To Immediate" )
		@ArgumentsSource ( AddressProvider.AddrRange.Negative.class )
		@CsvSource ( { "0xFFFFFFFF, -1" } )
		void testInvalid_Negative_Add2Imm (String hex, Integer addr) {
			assertThrows( IllegalArgumentException.class, ( ) -> Convert.address2Imm( addr ) );
		}
		
		@ParameterizedTest ( name="[{index}] - Fail Convert Invalid Immediate: \"{1}\" To Address" )
		@ArgumentsSource ( ImmediateProvider.ConvertInvalid.OutOfRange.class )
		void testInvalid_Immediate2Address (String hex, Integer imm) {
			assertThrows( IllegalArgumentException.class, ( ) -> Convert.imm2Address( imm ) );
		}
		
	}
	
	@Nested
	@DisplayName ( "Address --> Index" )
	class Address_To_Index {
		@ParameterizedTest ( name="[{index}] - Convert Instr Address\"[{0}, {1}]\" To Index \"{2}\"" )
		@ArgumentsSource ( AddressProvider.InstrAddr.Supported.class )
		void Instr_AddressToIndex (String hexAddr, Integer address, Integer index) {
			assertEquals( index, Convert.instrAddr2Index( address ) );
		}
		
		@ParameterizedTest ( name="[{index}] - Convert Invalid Instr Address\"[{0}, {1}]\" To Index" )
		@ArgumentsSource ( AddressProvider.InstrAddr.Valid.class )
		@ArgumentsSource ( AddressProvider.InstrAddr.NotValid.class )
		void Invalid_Instr_AddressToIndex (String hexAddr, Integer address) {
			assertThrows( IllegalArgumentException.class, ( ) -> Convert.instrAddr2Index( address ) );
		}
		
		@ParameterizedTest ( name="[{index}] - Convert Address\"[{0}, {1}]\" To Index \"{2}\" - Data" )
		@ArgumentsSource ( AddressProvider.DataAddr.Supported.class )
		void Data_AddressToIndex (String hexAddr, Integer address, Integer index) {
			assertEquals( index, Convert.dataAddr2Index( address ) );
		}
		
		@ParameterizedTest ( name="[{index}] - Convert Invalid Data Address\"[{0}, {1}]\" To Index" )
		@ArgumentsSource ( AddressProvider.DataAddr.Valid.class )
		@ArgumentsSource ( AddressProvider.DataAddr.NotValid.class )
		void Invalid_Data_AddressToIndex (String hexAddr, Integer address) {
			assertThrows( IllegalArgumentException.class, ( ) -> Convert.dataAddr2Index( address ) );
		}
		
	}
	
	@Nested
	class Register_Conversion {
		final int Index=0, R=1, Named=2;
		/** (Index, R, Named, $index, $R, $Named) */
		List<String[]> registers=RegisterProvider.getRegisters( );
		@Test
		void R_to_Named ( ) {
			registers.forEach( arr -> assertEquals(
					arr[ Named ], Convert.r2Named( arr[ R ] )
			) );
		}
		
		@Test
		void Named_to_R ( ) {
			registers.forEach( arr -> assertEquals(
					arr[ R ], Convert.named2R( arr[ Named ] )
			) );
		}
		
		@Test
		void R_to_Index ( ) {
			registers.forEach( arr -> assertEquals(
					Integer.parseInt( arr[ Index ] ), Convert.r2Index( arr[ R ] )
			) );
		}
		
		@Test
		void Named_to_Index ( ) {
			registers.forEach( arr -> assertEquals(
					Integer.parseInt( arr[ Index ] ), Convert.r2Index( Convert.named2R( arr[ Named ] ) )
			) );
		}
		
		@Test
		void Index_to_R ( ) {
			for ( int i=0; i<registers.size( ); i++ ) {
				assertEquals( registers.get( i )[ R ], Convert.index2R( i ) );
			}
		}
		
		@Test
		void Index_to_Named ( ) {
			for ( int i=0; i<registers.size( ); i++ ) {
				assertEquals( registers.get( i )[ Named ], Convert.r2Named( Convert.index2R( i ) ) );
			}
		}
		
		@Nested
		class ThrowsIllegalArgumentException {
			
			@Test
			void r2Named ( ) {
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.r2Named( "panda" ) );
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.r2Named( "r32" ) );
			}
			@Test
			void named2R ( ) {
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.named2R( "panda" ) );
			}
			@Test
			void r2Index ( ) {
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.r2Index( "panda" ) );
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.r2Index( "r-1" ) );
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.r2Index( "-r1" ) );
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.r2Index( "r32" ) );
				
				
			}
			
			@Test
			void index2R ( ) {
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.index2R( -1 ) );
				assertThrows( IllegalArgumentException.class, ( ) -> Convert.index2R( 32 ) );
			}
			
		}
		
	}
	
}
