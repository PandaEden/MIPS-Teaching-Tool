package util.validation;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test_util.Var;

import model.DataType;
import model.instr.Operands;

import util.logs.ErrorLog;
import util.logs.WarningsLog;

import static java.lang.Integer.MAX_VALUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static util.Convert.uInt2Hex;

@Tag("Utility")
@Tag("Validation")
@Tag( "Instructions" )
@DisplayName ("Utility : Validation - Instructions Test")
public class ValidateInstructionsTest {
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
	
	
	//OpCodes add, sub, addi, lw, sw, j, jal, halt, exit
	@ParameterizedTest
	@ValueSource (strings={ "add", "sub", "addi", "lw", "sw", "j", "jal", "halt", "exit" })
	@DisplayName ("Validate Supported Opcodes")
	void validateSupportedOpcodes(String opcode) {
		assertTrue( validate.isValidOpCode( 1, opcode ) );
	}
	
	@ParameterizedTest
	@ValueSource (strings={ "panda", "l.d", "s.d", "la", "lui", "" })
	@DisplayName ("Validate Not Supported Opcodes")
	void validateNot_SupportedOpcodes(String opcode) {
		assertFalse( validate.isValidOpCode( 230, opcode ) );
		var.errorMatches( "Opcode: \"" + opcode + "\" Not Supported!", 230 );
	}
	
	
	@Nested
	@DisplayName ("Validate Operands")
	class Val_Operands {
		private final DataType type=DataType.NORMAL;
		private WarningsLog warningsLog;
		
		@BeforeEach
		void setUp() {
			warningsLog=var.warningsLog;
		}
		
		private final String NO_OPS="\tNo Operands found !";
		
		private String opsForOpcodeNotValid(String operands, String opcode) {
			return "Operands: [" + operands + "] for Opcode: \"" + opcode + "\" Not Valid !";
		}
		
		private String regNotRecognized(String reg) {
			return "\tRegister: \"" + reg + "\" Not Recognised!";
		}
		
		@Test
		@DisplayName ("Test isValidOperands Invalid Opcode")
		void testIsValidOperands_InvalidOpcode() {
			assertNull( validate.splitValidOperands( 230, "panda", null, warningsLog ) );
			var.errorsMatch( new String[] { NO_OPS, opsForOpcodeNotValid( null, "panda" ) }, 230 );
		}
		
		@Test
		@DisplayName ("Test isValidOperands Null Opcode")
		void testIsValidOperands_NullOpcode() {
			assertNull( validate.splitValidOperands( 230, null, null, warningsLog ) );
		}
		
		@Test
		@DisplayName ("Test Operands, EXIT")
		void testOperands_EXIT() {
			Operands operands=validate.splitValidOperands( 12, "exit", null, warningsLog );
			assertNotNull( operands );
			assertAll(
					() -> assertNull( operands.getLabel( ) ),
					() -> assertNull( operands.getImmediate( ) ),
					() -> assertNull( operands.getRd( ) ),
					() -> assertNull( operands.getRs( ) ),
					() -> assertNull( operands.getRt( ) ),
					() -> assertEquals( Operands.InstrType.R, operands.getInstrType( ) )
			);
			
			Operands operands2=validate.splitValidOperands( 20, "halt", null, warningsLog );
			assertNotNull( operands2 );
			assertAll(
					() -> assertNull( operands2.getLabel( ) ),
					() -> assertNull( operands2.getImmediate( ) ),
					() -> assertNull( operands2.getRd( ) ),
					() -> assertNull( operands2.getRs( ) ),
					() -> assertNull( operands2.getRt( ) ),
					() -> assertEquals( Operands.InstrType.R, operands2.getInstrType( ) )
			);
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Exit")
		void testInvalid_Operands_Exit() {
			Operands operands=validate.splitValidOperands( 12, "exit", "$0, $0, $0", warningsLog );
			assertNull( operands );
			var.errorMatches( opsForOpcodeNotValid( "$0, $0, $0", "exit" ), 12 );
		}
		
		//-------------------------------------------------------------------
		// Testing add with good values
		@Test
		@DisplayName ("Test Operands, ADD")
		void testOperands_Add() {
			Operands operands=validate.splitValidOperands( 12, "add", "$8,r31, $s2", warningsLog );
			assertNotNull( operands );
			
			assertAll(
					() -> assertNull( operands.getLabel( ) ),
					() -> assertNull( operands.getImmediate( ) ),
					() -> assertEquals( Operands.InstrType.R, operands.getInstrType( ) ),
					() -> assertEquals( 8, operands.getRd( ) ),
					() -> assertEquals( 31, operands.getRs( ) ),
					() -> assertEquals( 18, operands.getRt( ) )
			);
		}
		
		@Test
		@DisplayName ("Test Operands, RType_Spacing")
		void testOperands_R_Type_Spacing() {
			Operands operands=validate.splitValidOperands( 12, "sub", "$24 , $s4, $s0", warningsLog );
			assertNotNull( operands );
			
			assertAll(
					() -> assertNull( operands.getLabel( ) ),
					() -> assertNull( operands.getImmediate( ) ),
					() -> assertEquals( Operands.InstrType.R, operands.getInstrType( ) ),
					() -> assertEquals( 24, operands.getRd( ) ),
					() -> assertEquals( 20, operands.getRs( ) ),
					() -> assertEquals( 16, operands.getRt( ) )
			);
		}
		
		@Test
		@DisplayName ("Test Operands, SUB")
		void testOperands_Sub() {
			Operands operands=validate.splitValidOperands( 30, "sub", "$zero, 31, $s2", warningsLog );
			// Second operands "31" should be invalid
			// Zero Write Warning
			assertAll(
					() -> assertNull( operands ),
					() -> var.errorsMatch( new String[] {
							regNotRecognized( "31" ),
							opsForOpcodeNotValid( "$zero, 31, $s2", "sub" )
					}, 30 ),
					() -> var.zeroWarning( 30, "$zero" )
			);
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Multiple")
		void testInvalid_Operands_Multiple() {
			Operands operands=validate.splitValidOperands( 30, "add", "$panda, 31, $ss", warningsLog );
			// Errors with all Operands
			assertAll(
					() -> assertNull( operands ),
					() -> var.errorsMatch( new String[] {
							regNotRecognized( "$panda" ), regNotRecognized( "31" ), regNotRecognized( "$ss" ),
							opsForOpcodeNotValid( "$panda, 31, $ss", "add" )
					}, 30 )
			);
		}
		
		@Test
		@DisplayName ("Test Operands, Immediate")
		void testOperandsImmediate() {
			Operands operands=validate.splitValidOperands( 72, "addi", "$1, r20, 75", warningsLog );
			assertNotNull( operands );
			
			assertAll(
					() -> assertNull( operands.getLabel( ) ),
					() -> assertEquals( 75, operands.getImmediate( ) ),
					() -> assertEquals( Operands.InstrType.I_write, operands.getInstrType( ) ),
					() -> assertNull( operands.getRd( ) ),
					() -> assertEquals( 20, operands.getRs( ) ),
					() -> assertEquals( 1, operands.getRt( ) )
			);
			
			Operands operands2=validate.splitValidOperands( 72, "addi", "$1, r20, $s3", warningsLog );
			assertNull( operands2 );
			var.errorMatches( "LineNo: 72\t\tImmediate Value: \"$s3\" Not Valid Integer!\n" +
							  "\tLineNo: 72\tOperands: [$1, r20, $s3] for Opcode: \"addi\" Not Valid !" );
			
			Operands operands3=validate.splitValidOperands( 52, "addi", "$1, 0x20", warningsLog );
			assertNull( operands3 );
			var.errorMatches( "Operands: [$1, 0x20] for Opcode: \"addi\" Not Valid !", 52 );
		}
		
		@Test
		@DisplayName ("Test Operands, Load")
		void testOperandsLoad() {
			Operands operands=validate.splitValidOperands( 0, "lw", "$0, 20($1)", warningsLog );
			assertNotNull( operands );
			
			var.zeroWarning( 0, "$0" );
			
			Operands operands2=validate.splitValidOperands( 5, "lw", "$2, panda", warningsLog );
			assertNotNull( operands2 );
		}
		
		@Test
		@DisplayName ("Test Operands, Store")
		void testOperandsStore() {
			Operands operands=validate.splitValidOperands( 0, "sw", "$0, 20($1)", warningsLog );
			assertNotNull( operands );
			
			// noWarnings();
			Operands operands2=validate.splitValidOperands( 5, "sw", "$2, _panda", warningsLog );
			assertNotNull( operands2 );
		}
		
		@Test
		@DisplayName ("Test Operands, Base+Offset")
		void testOperandsBaseOffset() {
			Operands operands=validate.splitValidOperands( 0, "sw", "$0, 20 ($1)", warningsLog );
			assertNotNull( operands );
			
			Operands operands2=validate.splitValidOperands( 0, "sw", "$0, ($1)", warningsLog );
			assertNotNull( operands2 );
		}
		
		@Test
		@DisplayName ("Test Operands, Base+Offset Hex")
		void testOperandsBaseOffsetHex() {
			Operands operands=validate.splitValidOperands( 0, "sw", "$0, 0x290($8)", warningsLog );
			assertNotNull( operands );
			assertEquals( 656, operands.getImmediate( ) );
		}
		
		@Test
		@DisplayName ("Test Operands, Jump")
		void testOperandsJump() {
			Operands operands=validate.splitValidOperands( 0, "j", "0x100009", warningsLog );
			assertNotNull( operands );
			
			Operands operands1=validate.splitValidOperands( 0, "jal", "cat", warningsLog );
			assertNotNull( operands1 );
		}
		
		@ParameterizedTest
		@ValueSource (ints={ (MAX_VALUE - 50), MAX_VALUE, (MAX_VALUE/4) + 1, -32769 })
		@DisplayName ("Test Invalid Operands, Jump _Immediate Out Of Range")
		void testInvalid_OperandsJump_ImmOutOfRange(int address) {
			Operands operands=validate.splitValidOperands( 0, "j", "" + address, warningsLog );
			assertNull( operands );
			
			var.errorMatches( "LineNo: 0\tImmediate Value: \"" + address + "\", Cannot Be Converted To A Valid Address!\n"
							  + "\tLineNo: 0\tOperands: [" + address + "] for Opcode: \"j\" Not Valid !" );
		}
		
		// Immediate Values (-2^15) to (2^29-1) Convert To Valid Addresses, but not Valid for Jump
		// Jump Imm is limited to (2^20) to ((2^20 + 2^18))
		@DisplayName ("Test Invalid Operands, Jump _Valid-NotSupported Immediate")
		@ParameterizedTest (name="{index} - {arguments} : InvalidOp Jump IMM")
		@ValueSource (ints={ -32768, (MAX_VALUE/4), // (-2^15) and (2^29-1) Boundaries
							 0x0FFFFF, 0x140001,        // Out of Supp Jump Imm Boundaries (2^20)-1 and (2^20 + 2^18)+1
							 0, 0x500004, 0x4000000 - 1 })
		// Extra Invalid Values
		void testInvalid_OperandsJump_ValImm(int address) {
			Operands operands=validate.splitValidOperands( 0, "j", "" + address, warningsLog );
			
			assertNull( operands );
			int a=address*4;
			String err="Instruction Address: \"" + uInt2Hex( a ) + "\" Not ";
			if ( a>=0x10000000 || a<0x00400000 ) {
				err+="Valid!\n";        // -32768, Integer.MAX_VALUE/4, 0x0FFFFF, 0
			} else {
				err+="Supported!\n";
			}
			var.errorMatches( err + "\tLineNo: 0\tOperands: [" + address + "] for Opcode: \"j\" Not Valid !" );
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Jump _TooManyOperands")
		void testInvalid_OperandsJump_TooManyOperands() {
			// Invalid - Too many Operands
			Operands operands=validate.splitValidOperands( 0, "j", "0x100009, 50", warningsLog );
			assertNull( operands );
			var.errorMatches( "LineNo: 0	Operands: [0x100009, 50] for Opcode: \"j\" Not Valid !" );
		}
		
		@Test
		@DisplayName ("Test Valid Label Address")
		void testValidLabelAddress() {
			assertTrue( util.validation.Val_Operands.isValidLabelOrInt( 7, "0x100000", errLog ) );
			
			assertFalse( util.validation.Val_Operands.isValidLabelOrInt( 8, "panda?", errLog ) );
			var.errorMatches( "LineNo: 8\tLabel: \"panda?\" Not Supported!" );
			
			assertTrue( util.validation.Val_Operands.isValidLabelOrInt( 8, "20", errLog ) );
			assertTrue( util.validation.Val_Operands.isValidLabelOrInt( 9, "2", errLog ) );
			
			assertFalse( util.validation.Val_Operands.isValidLabelOrInt( 20, "2_", errLog ) );
			var.errorMatches( "LineNo: 20\t\tImmediate Value: \"2_\" Not Valid Integer!" );
			
			assertTrue( util.validation.Val_Operands.isValidLabelOrInt( 9, "_main", errLog ) );
		}
		
		//validateLoadRegister
		@Test
		@DisplayName ("Validate Convert Register")
		void validateConvertRegister() {
			assertNull( util.validation.Val_Operands.convertRegister( 30, "$0", DataType.FLOATING_POINT, errLog ) );
			//assertNull(i);
			var.errorMatches( "LineNo: 30\t\tRegister: \"$0\" Wrong DataType!" );
			errLog.clear( );
			
			Integer z=util.validation.Val_Operands.convertRegister( 50, "$-40", DataType.NORMAL, errLog );
			assertNull( z );
			
			var.errorMatches( "LineNo: 50\t\tRegister: \"$-40\" Not In Range!" );
			errLog.clear( );
			
			Integer x=util.validation.Val_Operands.convertRegister( 20, "$50", DataType.NORMAL, errLog );
			assertNull( x );
			var.errorMatches( "LineNo: 20\t\tRegister: \"$50\" Not In Range!" );
		}
		
		//zeroRegister warning
		@ParameterizedTest
		@ValueSource (strings={ "$zero", "zero", "$r0", "r0", "$0" })
		@DisplayName ("Validate Load Register with Zero")
		void validateZeroLoadRegister(String text) {
			//isValidLoadRegister
			Assertions.assertAll(
					() -> Assertions.assertEquals( 0, util.validation.Val_Operands.convertRegister( 7, text, type, errLog ) ),
					// convert Register should create no warnings, only convert Write register should!
					() -> Assertions.assertEquals( 0, util.validation.Val_Operands.convertWriteRegister( 7, text, type, errLog, warningsLog ) ),
					() -> var.zeroWarning( 7, text )
			);
		}
		
	}
	
}
