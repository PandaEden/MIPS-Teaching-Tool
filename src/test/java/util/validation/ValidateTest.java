package util.validation;

import _test.providers.ImmediateProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import _test.TestLogs;
import _test.TestLogs.FMT_MSG;
import _test.Tags;
import _test.Tags.Pkg;

import _test.providers.AddressProvider.InstrAddr;
import _test.providers.AddressProvider.DataAddr;
import _test.providers.BlankProvider;
import _test.providers.SetupProvider;


import util.logs.ErrorLog;
import static org.junit.jupiter.api.Assertions.*;
import static util.validation.AddressValidation.*;

@Tag(Pkg.UTIL)
@Tag(Pkg.VALID)
@DisplayName (Pkg.UTIL+" : "+Pkg.VALID+" : Validate Test")
class ValidateTest {
	private static final TestLogs testLogs=new TestLogs( );
	private static ErrorLog errLog;
	private static ErrorLog expectedErrs;
	private static Validate validate;
	
	@BeforeAll
	static void beforeAll() {
		errLog=testLogs.actualErrors;
		expectedErrs = testLogs.expectedErrors;
		validate=new Validate( errLog );
	}
	
	@AfterEach
	void clear() {testLogs.after();}
	
	@Nested
	@Tag (Tags.ADDR)
	@DisplayName (Tags.ADDR)
	class Addresses {
		
		@Nested
		@Tag (Tags.INSTR)
		@DisplayName (Tags.INSTR)
		class Instructions {
			String instrAddressNot (String hexAddress, String thing) {
				return TestLogs.FMT_MSG.xAddressNot( "Instruction", hexAddress, thing );
			}
			
			@ParameterizedTest (name="Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (InstrAddr.Supported.class)
			void addrValTestSupported(@SuppressWarnings ("unused") String hexAddr, long address) {
				assertTrue( isSupportedInstrAddr( (int) address, errLog ) );
				assertEquals( (((int) address - 4194304)/4), instrAddr2index( (int) address, errLog ) );
			}
			
			@ParameterizedTest (name="Not_Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (InstrAddr.Valid.class)
			void addrValTestValid_NotSupported(String hexAddr, long address) {
				assertFalse( isSupportedInstrAddr( (int) address, errLog ) );
				expectedErrs.appendEx( instrAddressNot( hexAddr, "Supported" ));
				assertNull( instrAddr2index( (int) address, errLog ) );
				expectedErrs.appendEx( instrAddressNot( hexAddr, "Supported" ));
			}
			
			@ParameterizedTest (name="Not_Valid[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (InstrAddr.NotValid.class)
			void notValid(String hexAddr, long address) {
				assertFalse( isSupportedInstrAddr( (int) address, errLog ) );
				expectedErrs.appendEx( instrAddressNot( hexAddr, "Valid" ));
				assertNull( instrAddr2index( (int) address, errLog ) );
				expectedErrs.appendEx( instrAddressNot( hexAddr, "Valid" ));
			}
			
		}
		
		@Nested
		@Tag (Tags.DATA)
		@DisplayName (Tags.DATA)
		class Data {
			String dataAddressNot (String hexAddress, String thing) { return TestLogs.FMT_MSG.xAddressNot("Data",hexAddress,thing ); }
			
			@ParameterizedTest (name="Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (DataAddr.Supported.class)
			void addrValTestSupported(@SuppressWarnings ("unused") String hexAddr, long address) {
				assertTrue( isSupportedDataAddr( (int) address, errLog ) );
				assertEquals( (((int) address - 268500992)/4), dataAddr2index( (int) address, errLog ) );
			}
			
			@ParameterizedTest (name="Not_Supported[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (DataAddr.Valid.class)
			void addrValTestValid_NotSupported(String hexAddr, long address) {
				assertFalse( isSupportedDataAddr( (int) address, errLog ) );
				expectedErrs.appendEx( dataAddressNot( hexAddr, "Supported" ));
				assertNull( dataAddr2index( (int) address, errLog ) );
				expectedErrs.appendEx( dataAddressNot( hexAddr, "Supported" ));
			}
			
			void assertNotValidNotAligned(String hexAddress, boolean aligned) {
				if ( !aligned )
					expectedErrs.appendEx( dataAddressNot( hexAddress, "DoubleWord Aligned" ));
				expectedErrs.appendEx( dataAddressNot( hexAddress, "Valid" ));
			}
			
			@ParameterizedTest (name="Not_Valid[{index}] - Address: \"{arguments}\"")
			@ArgumentsSource (DataAddr.NotValid.class)
			void notValid(String hexAddr, int address) {
				boolean aligned=address%8==0;
				
				assertFalse( isSupportedDataAddr( (int) address, errLog ) );
				assertNotValidNotAligned( hexAddr, aligned );
				assertNull( dataAddr2index( (int) address, errLog ) );
				assertNotValidNotAligned( hexAddr, aligned );
			}
			
		}
		@Nested
		@Tag (Tags.I_ADDR)
		@DisplayName (Tags.I_ADDR)
		class Immediate {
			
			@ParameterizedTest (name="{index}Valid imm2Address[{arguments}]")
			@ArgumentsSource ( ImmediateProvider.All.class)
			void valid(String hexAddr, int address, String hexImm, int imm) {
				assertEquals(address, convertValidImm2Addr( 150, imm, errLog));
			}
			
			@ParameterizedTest (name="Not_Valid imm2Address[{index}] - Immediate: \"{arguments}\"")
			@ArgumentsSource ( ImmediateProvider.ConvertInvalid.Boundary.class)
			void invalid(String hexImm, int imm) {
				assertNull(convertValidImm2Addr( 150, imm, errLog));
				expectedErrs.appendEx( 150, FMT_MSG.imm.cantConvert( imm ) );
			}
		}
	}
	
	@Nested
	@Tag (Tags.STP)
	@DisplayName (Tags.STP)
	class Setup {
		//Directives .data .text .code
		@ParameterizedTest (name="Supported[{index}] - Directive: \"{arguments}\"")
		@ArgumentsSource (SetupProvider.ValidDirectives.class)
		void directiveSupported(String directive) {
			assertTrue( validate.isValidDirective( 5, directive ) );
		}
		
		@ParameterizedTest (name="Not_Supported[{index}] - Directive: \"{arguments}\"")
		@ArgumentsSource (BlankProvider.Null.class)
		@ArgumentsSource (SetupProvider.InvalidDirectives.class)
		void directiveNot_Supported(String directive) {
			assertFalse( validate.isValidDirective( 10, directive ) );
			expectedErrs.appendEx( 10, FMT_MSG.DirectiveNotSupported( directive ));
		}
		
		//DataTypes .word
		@ParameterizedTest (name="Supported[{index}] - DataType: \"{arguments}\"")
		@ArgumentsSource (SetupProvider.ValidDataTypes.class)
		void dataTypeSupported(String type) {
			assertTrue( validate.isValidDirective( 0, type ) );
			assertTrue( Validate.isDataType( type ) );
		}
		
		@ParameterizedTest (name="Not_Supported[{index}] - DataType: \"{arguments}\"")
		@ArgumentsSource (BlankProvider.Null.class)
		@ArgumentsSource (SetupProvider.InvalidDataTypes.class)
		void dataTypeNot_Supported(String type) {
			assertFalse( Validate.isDataType( type ) );
		}
		
		//Labels _[a-z] separators [_.\-a-z\d]*
		@ParameterizedTest (name="Supported[{index}] - Label: \"{arguments}\"")
		@ArgumentsSource (SetupProvider.ValidLabels.class)
		void labelsSupported(String label) {
			assertNotNull( validate.isValidLabel( 40, label ) );
		}
		
		@ParameterizedTest (name="Not_Supported[{index}] - Label: \"{arguments}\"")
		@ArgumentsSource (BlankProvider.NwLn.class)
		@ArgumentsSource (SetupProvider.InvalidLabels.class)
		void labelsNot_Supported(String label) {
			assertNull( validate.isValidLabel( 72, label ) );
			expectedErrs.appendEx(72, FMT_MSG.label.notSupp( label ));
		}
		
	}
	
}
