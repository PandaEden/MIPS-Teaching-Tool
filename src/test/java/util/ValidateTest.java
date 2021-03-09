package util;

import model.DataType;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.instr.Operands;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import util.logs.ErrorLog;
import util.logs.WarningsLog;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ValidateTest{
	private static ErrorLog errLog;
	private static Validate validate;
	
	@BeforeAll
	static void beforeAll(){
		errLog = new ErrorLog(new ArrayList<>());
		validate = new Validate(errLog);
	}
	
	@AfterEach
	void clear(){
		//noErrors();		// Should be no Errors Not checked using assertError
		errLog.clear();
	}
	
	void noErrors(){
		Assertions.assertAll(
				() -> assertFalse(errLog.hasEntries()),
				() -> assertEquals("", errLog.toString())
		);
	}
	
	void assertError(String msg){
		if (!msg.isBlank()) {
			Assertions.assertAll(
					() -> assertTrue(errLog.hasEntries()),
					() -> assertEquals("Errors:\n\t"+msg+"\n", errLog.toString())
			);
		}
		errLog.clear();
	}
	
	@Test
	@DisplayName ("static variables are correct")
	void staticVars(){
		Assertions.assertAll(
				() -> assertEquals(512, Validate.MAX_FILE_LINES),
				() -> assertEquals(256, InstrMemory.MAX_INSTR_COUNT),
				() -> assertEquals(4194304, InstrMemory.BASE_INSTR_ADDRESS),
				() -> assertEquals(5242880, InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS),
				() -> assertEquals(268435456, InstrMemory.OVER_INSTR_ADDRESS),
				() -> assertEquals(268500992,
						DataMemory.BASE_DATA_ADDRESS),
				() -> assertEquals(268503040, DataMemory.OVER_SUPPORTED_DATA_ADDRESS),
				() -> assertEquals(268697600, DataMemory.OVER_DATA_ADDRESS),
				() -> assertEquals(8, DataMemory.DATA_ALIGN)
		);
	}
	
	@Nested
	@DisplayName ("Address Validation Tests")
	class AddressValidationTests{
		//------INSTR ADDRESSES--------------
		@ParameterizedTest
		@ValueSource (longs = {0x00400000L, 0x004003E4L, 0x004FFFFCL, 0x00500000L-4})
		@DisplayName ("Validate - Supported Instr Address")
		void validateSupportedInstrAddress(long address){
			assertTrue(AddressValidation.isSupportedInstrAddr((int) address, errLog));
			assertEquals(Convert.address2Index((int) address), AddressValidation.addr2index((int) address, true, errLog));
		}
		
		@ParameterizedTest
		@ValueSource (longs = {0x00500000L, 0x00500004L, 0x08000000L, 0x0FFFFFFCL, 0x01400010,})
		@DisplayName ("Validate - Not_Supported Instr Address")
		void validateNot_SupportedInstrAddress(long address){
			assertFalse(AddressValidation.isSupportedInstrAddr((int) address, errLog));
			assertError("Instruction Address: \""+Convert.uInt2Hex((int) address)+"\" Not Supported!");
			
			assertNull(AddressValidation.addr2index((int) address, true, errLog));
			assertError("Instruction Address: \""+Convert.uInt2Hex((int) address)+"\" Not Supported!");
		}
		
		@ParameterizedTest
		@ValueSource (ints = {Integer.MIN_VALUE, -0x00400000, 0, 0x00400000-4, 0x00400003,
				0x10000000, 0x10010000, Integer.MAX_VALUE})
		@DisplayName ("Validate - Not_-Valid Instr Address")
		void validateNot_ValidInstrAddress(int address){
			assertFalse(AddressValidation.isSupportedInstrAddr(address, errLog));
			assertError("Instruction Address: \""+Convert.uInt2Hex(address)+"\" Not Valid!");
			
			assertNull(AddressValidation.addr2index(address, true, errLog));
			assertError("Instruction Address: \""+Convert.uInt2Hex(address)+"\" Not Valid!");
		}
		
		//------DATA ADDRESSES---------------
		@ParameterizedTest
		@ValueSource (longs = {0x10010000L, 0x10010020L, 0x10010800L-8})
		@DisplayName ("Validate - Supported Data Address")
		void validateSupportedDataAddress(long address){
			assertTrue(AddressValidation.isSupportedDataAddr((int) address, errLog));
			assertEquals(Convert.address2Index((int) address),
					AddressValidation.addr2index((int) address, false, errLog));
		}
		
		@ParameterizedTest
		@ValueSource (longs = {0x10010800L, 0x10020000L, 0x10040000L-8})
		@DisplayName ("Validate - Not_Supported-Valid Data Address")
		void validateNot_SupportedDataAddress(long address){
			assertFalse(AddressValidation.isSupportedDataAddr((int) address, errLog));
			assertError("Data Address: \""+Convert.uInt2Hex((int) address)+"\" Not Supported!");
			
			assertNull(AddressValidation.addr2index((int) address, false, errLog));
			assertError("Data Address: \""+Convert.uInt2Hex((int) address)+"\" Not Supported!");
		}
		
		@Test
		@DisplayName ("Validate - Invalid Data Address_Under")
		void validateInvalid_DataAddressUnder(){
			assertFalse(AddressValidation.isSupportedDataAddr(0x00400000, errLog));
			assertError("Data Address: \"0x00400000\" Not Valid!");
			
			assertNull(AddressValidation.addr2index(0x10000000, false, errLog));
			assertError("Data Address: \"0x10000000\" Not Valid!");
		}
		
		@Test
		@DisplayName ("Validate - Invalid Data Address_Over")
		void validateInvalid_DataAddressOver(){
			assertFalse(AddressValidation.isSupportedDataAddr((int) 0x7FFFFFF8L, errLog));
			assertError("Data Address: \"0x7FFFFFF8\" Not Valid!");
			
			assertNull(AddressValidation.addr2index((int) 0x78000000L, false, errLog));
			assertError("Data Address: \"0x78000000\" Not Valid!");
		}
		
		@Test
		@DisplayName ("Validate - Data Address not DoubleWord Aligned")
		void validateDataAddressNotDoubleWordAligned(){
			assertFalse(AddressValidation.isSupportedDataAddr((int) 0x10010004L, errLog));
			assertError("Data Address: \"0x10010004\" Not DoubleWord Aligned!");
		}
	}
	
	@Nested
	@DisplayName ("Non-Operands")
	class nonOperands{
		//Directives .data .text .code
		@ParameterizedTest
		@ValueSource (strings = {".data", ".text", ".code", ".word"})
		@DisplayName ("Validate Supported Directive")
		void validateSupportedDirective(String directive){
			assertTrue(validate.isValidDirective(5, directive));
		}
		
		@ParameterizedTest
		@ValueSource (strings = {".global", ".extern", ".DATA"})
		@DisplayName ("Validate Not Supported Directive")
		void validateNot_SupportedDirective(String directive){
			assertFalse(validate.isValidDirective(10, directive));
			assertError("LineNo: 10\tDirective: \""+directive+"\" Not Supported!");
		}
		
		//DataTypes .word
		@ParameterizedTest
		@ValueSource (strings = {".word"})
		@DisplayName ("Validate Supported DataType")
		void validateSupportedDataType(String type){
			assertTrue(validate.isValidDirective(0, type));
			assertTrue(validate.isDataType(type));
		}
		
		@ParameterizedTest
		@ValueSource (strings = {".double", ".half", ".byte", ".float", ".panda", ".WORD", ".wo rd",
				".data", ".text", ".code"})
		@DisplayName ("Validate Not Supported DataType")
		void validateNotSupportedDataType(String type){
			assertFalse(validate.isDataType(type));
		}
		
		//Labels _[a-z] separators ['.','-','_',a-z]*
		@ParameterizedTest
		@ValueSource (strings = {"_label", "label", "also_a.label-with_splits", "label0"})
		@DisplayName ("Validate Supported Labels")
		void validateSupportedLabels(String label){
			assertNotNull(validate.isValidLabel(40, label));
		}
		
		@ParameterizedTest
		@ValueSource (strings = {"_ label with spaces", "73label", "CAPITALIZED", ".data"})
		@DisplayName ("Validate Not Supported Labels")
		void validateNot_SupportedLabels(String label){
			assertNull(validate.isValidLabel(72, label));
			assertError("LineNo: 72\tLabel: \""+label+"\" Not Supported!");
		}
		
		@Test
		@DisplayName ("test Null Label")
		void testNullLabel(){
			assertNull(validate.isValidLabel(72, null));
		}
		
		//OpCodes add, sub, addi, lw, sw, j, jal, halt, exit
		@ParameterizedTest
		@ValueSource (strings = {"add", "sub", "addi", "lw", "sw", "j", "jal", "halt", "exit"})
		@DisplayName ("Validate Supported Opcodes")
		void validateSupportedOpcodes(String opcode){
			assertTrue(validate.isValidOpCode(1, opcode));
		}
		
		@ParameterizedTest
		@ValueSource (strings = {"panda", "l.d", "s.d", "la", "lui", ""})
		@DisplayName ("Validate Not Supported Opcodes")
		void validateNot_SupportedOpcodes(String opcode){
			assertFalse(validate.isValidOpCode(230, opcode));
			assertError("LineNo: 230\tOpcode: \""+opcode+"\" Not Supported!");
		}
	}
	
	@Nested
	@DisplayName ("Validate Operands")
	class Val_Operands{
		private final DataType type = DataType.NORMAL;
		private final WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
		
		void assertZeroWarning(int lineNo, String regName){
			Assertions.assertAll(
					() -> assertTrue(warningsLog.hasEntries()),
					() -> assertEquals("Warnings:\n\t"+"LineNo: "+lineNo+"\tDestination Register: \""+regName+"\" Cannot be modified!,"
							+"\t Result will be ignored!"+"\n", warningsLog.toString())
			);
			warningsLog.clear();
		}
		
		void noWarnings(){
			Assertions.assertAll(
					() -> assertFalse(warningsLog.hasEntries()),
					() -> assertEquals("", warningsLog.toString())
			);
		}
		
		@AfterEach
		void tearDown(){
			noWarnings();
			warningsLog.clear();
		}
		
		@Test
		@DisplayName ("Test isValidOperands Invalid Opcode")
		void testIsValidOperands_InvalidOpcode(){
			assertNull(validate.splitValidOperands(230, "panda", null, warningsLog));
			assertError("LineNo 230\t\tNo Operands found!\n"+
					"\tLineNo: 230\tOperands: [null] for Opcode: \"panda\" Not Valid !");
		}
		
		@Test
		@DisplayName ("Test isValidOperands Null Opcode")
		void testIsValidOperands_NullOpcode(){
			assertNull(validate.splitValidOperands(230, null, null, warningsLog));
		}
		
		@Test
		@DisplayName ("Test Operands, EXIT")
		void testOperands_EXIT(){
			Operands operands = validate.splitValidOperands(12, "exit", null, warningsLog);
			assertNotNull(operands);
			assertAll(
					() -> assertNull(operands.getLabel()),
					() -> assertNull(operands.getImmediate()),
					() -> assertNull(operands.getRd()),
					() -> assertNull(operands.getRs()),
					() -> assertNull(operands.getRt()),
					() -> assertEquals(Operands.InstrType.R, operands.getInstrType())
			);
			
			Operands operands2 = validate.splitValidOperands(20, "halt", null, warningsLog);
			assertNotNull(operands2);
			assertAll(
					() -> assertNull(operands2.getLabel()),
					() -> assertNull(operands2.getImmediate()),
					() -> assertNull(operands2.getRd()),
					() -> assertNull(operands2.getRs()),
					() -> assertNull(operands2.getRt()),
					() -> assertEquals(Operands.InstrType.R, operands2.getInstrType())
			);
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Exit")
		void testInvalid_Operands_Exit(){
			Operands operands = validate.splitValidOperands(12, "exit", "$0, $0, $0", warningsLog);
			assertNull(operands);
			
			assertError("LineNo: 12\tOperands: [$0, $0, $0] for Opcode: \"exit\" Not Valid !");
		}
		
		//-------------------------------------------------------------------
		// Testing add with good values
		@Test
		@DisplayName ("Test Operands, ADD")
		void testOperands_Add(){
			Operands operands = validate.splitValidOperands(12, "add", "$8,r31, $s2", warningsLog);
			assertNotNull(operands);
			
			assertAll(
					() -> assertNull(operands.getLabel()),
					() -> assertNull(operands.getImmediate()),
					() -> assertEquals(Operands.InstrType.R, operands.getInstrType()),
					() -> assertEquals(8, operands.getRd()),
					() -> assertEquals(31, operands.getRs()),
					() -> assertEquals(18, operands.getRt())
			);
		}
		
		@Test
		@DisplayName ("Test Operands, RType_Spacing")
		void testOperands_R_Type_Spacing(){
			Operands operands = validate.splitValidOperands(12, "sub", "$24 , $s4, $s0", warningsLog);
			assertNotNull(operands);
			
			assertAll(
					() -> assertNull(operands.getLabel()),
					() -> assertNull(operands.getImmediate()),
					() -> assertEquals(Operands.InstrType.R, operands.getInstrType()),
					() -> assertEquals(24, operands.getRd()),
					() -> assertEquals(20, operands.getRs()),
					() -> assertEquals(16, operands.getRt())
			);
		}
		
		@Test
		@DisplayName ("Test Operands, SUB")
		void testOperands_Sub(){
			Operands operands = validate.splitValidOperands(30, "sub", "$zero, 31, $s2", warningsLog);
			// Second operands "31" should be invalid
			// Zero Write Warning
			assertAll(
					() -> assertNull(operands),
					() -> assertError("LineNo: 30\t\tRegister: \"31\" Not Recognised!\n"
							+"\tLineNo: 30\tOperands: [$zero, 31, $s2] for Opcode: \"sub\" Not Valid !"),
					() -> assertZeroWarning(30, "$zero")
			);
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Multiple")
		void testInvalid_Operands_Multiple(){
			Operands operands = validate.splitValidOperands(30, "add", "$panda, 31, $ss", warningsLog);
			// Errors with all Operands
			assertAll(
					() -> assertNull(operands),
					() -> assertError("LineNo: 30\t\tRegister: \"$panda\" Not Valid!\n"
							+"\tLineNo: 30\t\tRegister: \"31\" Not Recognised!\n"
							+"\tLineNo: 30\t\tRegister: \"$ss\" Not Valid!\n"
							+"\tLineNo: 30\tOperands: [$panda, 31, $ss] for Opcode: \"add\" Not Valid !"
					)
			);
		}
		
		@Test
		@DisplayName ("Test Operands, Immediate")
		void testOperandsImmediate(){
			Operands operands = validate.splitValidOperands(72, "addi", "$1, r20, 75", warningsLog);
			assertNotNull(operands);
			
			assertAll(
					() -> assertNull(operands.getLabel()),
					() -> assertEquals(75, operands.getImmediate()),
					() -> assertEquals(Operands.InstrType.I_write, operands.getInstrType()),
					() -> assertNull(operands.getRd()),
					() -> assertEquals(20, operands.getRs()),
					() -> assertEquals(1, operands.getRt())
			);
			
			Operands operands2 = validate.splitValidOperands(72, "addi", "$1, r20, $s3", warningsLog);
			assertNull(operands2);
			assertError("LineNo: 72\t\tImmediate Value: \"$s3\" Not Valid Integer!\n"+
					"\tLineNo: 72\tOperands: [$1, r20, $s3] for Opcode: \"addi\" Not Valid !");
			
			Operands operands3 = validate.splitValidOperands(52, "addi", "$1, 0x20", warningsLog);
			assertNull(operands3);
			assertError("LineNo: 52\tOperands: [$1, 0x20] for Opcode: \"addi\" Not Valid !");
		}
		
		@Test
		@DisplayName ("Test Operands, Load")
		void testOperandsLoad(){
			Operands operands = validate.splitValidOperands(0, "lw", "$0, 20($1)", warningsLog);
			assertNotNull(operands);
			
			assertZeroWarning(0, "$0");
			
			Operands operands2 = validate.splitValidOperands(5, "lw", "$2, panda", warningsLog);
			assertNotNull(operands2);
		}
		
		@Test
		@DisplayName ("Test Operands, Store")
		void testOperandsStore(){
			Operands operands = validate.splitValidOperands(0, "sw", "$0, 20($1)", warningsLog);
			assertNotNull(operands);
			
			// noWarnings();
			Operands operands2 = validate.splitValidOperands(5, "sw", "$2, _panda", warningsLog);
			assertNotNull(operands2);
		}
		
		@Test
		@DisplayName ("Test Operands, Base+Offset")
		void testOperandsBaseOffset(){
			Operands operands = validate.splitValidOperands(0, "sw", "$0, 20 ($1)", warningsLog);
			assertNotNull(operands);
			
			Operands operands2 = validate.splitValidOperands(0, "sw", "$0, ($1)", warningsLog);
			assertNotNull(operands2);
		}
		
		@Test
		@DisplayName ("Test Operands, Base+Offset Hex")
		void testOperandsBaseOffsetHex(){
			Operands operands = validate.splitValidOperands(0, "sw", "$0, 0x290($8)", warningsLog);
			assertNotNull(operands);
			assertEquals(656, operands.getImmediate());
		}
		
		@Test
		@DisplayName ("Test Operands, Jump")
		void testOperandsJump(){
			Operands operands = validate.splitValidOperands(0, "j", "0x100009", warningsLog);
			assertNotNull(operands);
			
			Operands operands1 = validate.splitValidOperands(0, "jal", "cat", warningsLog);
			assertNotNull(operands1);
		}
		
		@ParameterizedTest
		@ValueSource (ints = {(Integer.MAX_VALUE-50), Integer.MAX_VALUE, (Integer.MAX_VALUE/4)+1, -32769})
		@DisplayName ("Test Invalid Operands, Jump _Immediate Out Of Range")
		void testInvalid_OperandsJump_ImmOutOfRange(int address){
			Operands operands = validate.splitValidOperands(0, "j", ""+address, warningsLog);
			assertNull(operands);
			
			assertError("LineNo: 0\tImmediate Value: \""+address+"\", Cannot Be Converted To A Valid Address!\n"
					+"\tLineNo: 0\tOperands: ["+address+"] for Opcode: \"j\" Not Valid !");
		}
		
		// Immediate Values (-2^15) to (2^29-1) Convert To Valid Addresses, but not Valid for Jump
		// Jump Imm is limited to (2^20) to ((2^20 + 2^18))
		@DisplayName ("Test Invalid Operands, Jump _Valid-NotSupported Immediate")
		@ParameterizedTest (name = "{index} - {arguments} : InvalidOp Jump IMM")
		@ValueSource (ints = {-32768, (Integer.MAX_VALUE/4), // (-2^15) and (2^29-1) Boundaries
				0x0FFFFF, 0x140001,        // Out of Supp Jump Imm Boundaries (2^20)-1 and (2^20 + 2^18)+1
				0, 0x500004, 0x4000000-1})
		// Extra Invalid Values
		void testInvalid_OperandsJump_ValImm(int address){
			Operands operands = validate.splitValidOperands(0, "j", ""+address, warningsLog);
			
			assertNull(operands);
			int a = address*4;
			String err = "Instruction Address: \""+Convert.uInt2Hex(a)+"\" Not ";
			if (a>=0x10000000 || a<0x00400000) {
				err += "Valid!\n";        // -32768, Integer.MAX_VALUE/4, 0x0FFFFF, 0
			} else {
				err += "Supported!\n";
			}
			assertError(err+"\tLineNo: 0\tOperands: ["+address+"] for Opcode: \"j\" Not Valid !");
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Jump _TooManyOperands")
		void testInvalid_OperandsJump_TooManyOperands(){
			// Invalid - Too many Operands
			Operands operands = validate.splitValidOperands(0, "j", "0x100009, 50", warningsLog);
			assertNull(operands);
			assertError("LineNo: 0	Operands: [0x100009, 50] for Opcode: \"j\" Not Valid !");
		}
		
		@Test
		@DisplayName ("Test Valid Label Address")
		void testValidLabelAddress(){
			assertTrue(util.Val_Operands.isValidLabelOrInt(7, "0x100000", validate, errLog));
			
			assertFalse(util.Val_Operands.isValidLabelOrInt(8, "panda?", validate, errLog));
			assertError("LineNo: 8\tLabel: \"panda?\" Not Supported!");
			
			assertTrue(util.Val_Operands.isValidLabelOrInt(8, "20", validate, errLog));
			assertTrue(util.Val_Operands.isValidLabelOrInt(9, "2", validate, errLog));
			
			assertFalse(util.Val_Operands.isValidLabelOrInt(20, "2_", validate, errLog));
			assertError("LineNo: 20\t\tImmediate Value: \"2_\" Not Valid Integer!");
			
			assertTrue(util.Val_Operands.isValidLabelOrInt(9, "_main", validate, errLog));
		}
		
		//validateLoadRegister
		@Test
		@DisplayName ("Validate Convert Register")
		void validateConvertRegister(){
			assertNull(util.Val_Operands.convertRegister(30, "$0", DataType.FLOATING_POINT, errLog));
			//assertNull(i);
			assertError("LineNo: 30\t\tRegister: \"$0\" Wrong DataType!");
			errLog.clear();
			
			Integer z = util.Val_Operands.convertRegister(50, "$-40", DataType.NORMAL, errLog);
			assertNull(z);
			
			assertError("LineNo: 50\t\tRegister: \"$-40\" Not In Range!");
			errLog.clear();
			
			Integer x = util.Val_Operands.convertRegister(20, "$50", DataType.NORMAL, errLog);
			assertNull(x);
			assertError("LineNo: 20\t\tRegister: \"$50\" Not In Range!");
		}
		
		//zeroRegister warning
		@ParameterizedTest
		@ValueSource (strings = {"$zero", "zero", "$r0", "r0", "$0"})
		@DisplayName ("Validate Load Register with Zero")
		void validateZeroLoadRegister(String text){
			//isValidLoadRegister
			Assertions.assertAll(
					() -> Assertions.assertEquals(0, util.Val_Operands.convertRegister(7, text, type, errLog)),
					this::noWarnings,
					// convert Register should create no warnings, only convert Write register should!
					() -> Assertions.assertEquals(0, util.Val_Operands.convertWriteRegister(7, text, type, errLog, warningsLog)),
					ValidateTest.this::noErrors,
					() -> assertZeroWarning(7, text)
			);
		}
	}
}