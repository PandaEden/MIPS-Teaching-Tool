package util.validation;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import test_util.Var;
import test_util.providers.AddressProvider.InstrAddr;
import test_util.providers.AddressProvider.DataAddr;
import test_util.providers.BlankProvider;
import test_util.providers.SetupProvider;
import util.logs.ErrorLog;
import static org.junit.jupiter.api.Assertions.*;
import static util.Convert.*;
import static util.validation.AddressValidation.*;

@Tag("Utility")
@Tag("Validation")
@DisplayName ("Utility : Validation - Validate Test")
class ValidateTest {
	private static final Var var=new Var( );
	private static ErrorLog errLog;
	private static Validate validate;
	
	@BeforeAll
	static void beforeAll() {
		errLog=var.errorLog;
		validate=new Validate( errLog );
	}
	
	@AfterEach
	void clear() {
		var.assertLogsAreEmpty( );
	}
	
	@Nested
	@Tag ("Addresses")
	@DisplayName ("Addresses")
	class Addresses {
		
		@Nested
		@Tag ("Instructions")
		@DisplayName ("Instructions")
		class Instructions {
			void assertAddressNot(String hexAddress, String thing) {
				var.errorMatches( "Instruction Address: \"" + hexAddress + "\" Not " + thing + "!" );
			}
			
			@ParameterizedTest (name="Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (InstrAddr.Supported.class)
			@DisplayName ("Supported")
			void addrValTestSupported(@SuppressWarnings ("unused") String hexAddr, long address) {
				assertTrue( isSupportedInstrAddr( (int) address, errLog ) );
				assertEquals( instrAddr2Index( (int) address ), addr2index( (int) address, true, errLog ) );
			}
			
			@ParameterizedTest (name="Not_Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (InstrAddr.Valid.class)
			@DisplayName ("Not_Supported")
			void addrValTestValid_NotSupported(String hexAddr, long address) {
				assertFalse( isSupportedInstrAddr( (int) address, errLog ) );
				assertAddressNot( hexAddr, "Supported" );
				assertNull( addr2index( (int) address, true, errLog ) );
				assertAddressNot( hexAddr, "Supported" );
			}
			
			@ParameterizedTest (name="Not_Valid[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (InstrAddr.NotValid.class)
			@DisplayName ("Not_Valid")
			void notValid(String hexAddr, long address) {
				assertFalse( isSupportedInstrAddr( (int) address, errLog ) );
				assertAddressNot( hexAddr, "Valid" );
				assertNull( addr2index( (int) address, true, errLog ) );
				assertAddressNot( hexAddr, "Valid" );
			}
			
		}
		
		@Nested
		@Tag ("Data")
		@DisplayName ("Data")
		class Data {
			void assertValidAddressNot(String hexAddress) {
				var.errorMatches( "Data Address: \"" + hexAddress + "\" Not " + "Supported" + "!" );
			}
			
			@ParameterizedTest (name="Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (DataAddr.Supported.class)
			@DisplayName ("Supported")
			void addrValTestSupported(@SuppressWarnings ("unused") String hexAddr, long address) {
				assertTrue( isSupportedDataAddr( (int) address, errLog ) );
				assertEquals( dataAddr2Index( (int) address ), addr2index( (int) address, false, errLog ) );
			}
			
			@ParameterizedTest (name="Not_Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (DataAddr.Valid.class)
			@DisplayName ("Not_Supported")
			void addrValTestValid_NotSupported(String hexAddr, long address) {
				assertFalse( isSupportedDataAddr( (int) address, errLog ) );
				assertValidAddressNot( hexAddr );
				assertNull( addr2index( (int) address, false, errLog ) );
				assertValidAddressNot( hexAddr );
			}
			
			void assertNotValidNotAligned(String hexAddress, boolean aligned) {
				String firstHalf="Data Address: \"" + hexAddress + "\" Not ";
				if ( aligned )
					var.errorMatches( firstHalf + "Valid" + "!" );
				else
					var.errorMatches( firstHalf + "DoubleWord Aligned!\n\t" + firstHalf + "Valid" + "!" );
			}
			
			@ParameterizedTest (name="Not_Valid[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (DataAddr.NotValid.class)
			@DisplayName ("Not_Valid")
			void notValid(String hexAddr, long address) {
				boolean aligned=address%8==0;
				
				assertFalse( isSupportedDataAddr( (int) address, errLog ) );
				assertNotValidNotAligned( hexAddr, aligned );
				assertNull( addr2index( (int) address, false, errLog ) );
				assertNotValidNotAligned( hexAddr, aligned );
			}
			
		}
		
	}
	
	@Nested
	@Tag ("Setup")
	@DisplayName ("Setup")
	class Setup {
		//Directives .data .text .code
		@ParameterizedTest (name="Supported[{index}] - Directive: \"{arguments}\"")
		@ArgumentsSource (SetupProvider.ValidDirectives.class)
		@DisplayName ("Supported Directive")
		void directiveSupported(String directive) {
			assertTrue( validate.directive( 5, directive ) );
		}
		
		@ParameterizedTest (name="Not_Supported[{index}] - Directive: \"{arguments}\"")
		@ArgumentsSource (BlankProvider.Null.class)
		@ArgumentsSource (SetupProvider.InvalidDirectives.class)
		@DisplayName ("Not Supported Directive")
		void directiveNot_Supported(String directive) {
			assertFalse( validate.directive( 10, directive ) );
			var.errorMatches( "Directive: \"" + directive + "\" Not Supported!", 10 );
		}
		
		//DataTypes .word
		@ParameterizedTest (name="Supported[{index}] - DataType: \"{arguments}\"")
		@ArgumentsSource (SetupProvider.ValidDataTypes.class)
		@DisplayName ("Supported DataType")
		void dataTypeSupported(String type) {
			assertTrue( validate.directive( 0, type ) );
			assertTrue( Validate.isDataType( type ) );
		}
		
		@ParameterizedTest (name="Not_Supported[{index}] - DataType: \"{arguments}\"")
		@ArgumentsSource (BlankProvider.Null.class)
		@ArgumentsSource (SetupProvider.InvalidDataTypes.class)
		@DisplayName ("Validate Not Supported DataType")
		void dataTypeNot_Supported(String type) {
			assertFalse( Validate.isDataType( type ) );
		}
		
		//Labels _[a-z] separators [_.\-a-z\d]*
		@ParameterizedTest (name="Supported[{index}] - Label: \"{arguments}\"")
		@ArgumentsSource (SetupProvider.ValidLabels.class)
		@DisplayName ("Validate Supported Labels")
		void labelsSupported(String label) {
			assertNotNull( validate.label( 40, label ) );
		}
		
		@ParameterizedTest (name="Not_Supported[{index}] - Label: \"{arguments}\"")
		@ArgumentsSource (BlankProvider.Minimal.class)
		@ArgumentsSource (SetupProvider.InvalidLabels.class)
		@DisplayName ("Validate Not Supported Labels")
		void labelsNot_Supported(String label) {
			assertNull( validate.label( 72, label ) );
			var.errorMatches( "Label: \"" + label + "\" Not Supported!", 72 );
		}
		
	}
	
}
