package util;

import model.DataType;
import model.instr.Operands;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import util.logs.ErrorLog;
import util.logs.WarningsLog;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ValidateTest{
	static ErrorLog errLog;
	static Validate validate;
	static Random random;
	
	@BeforeAll
	static void setUp(){
		errLog = new ErrorLog(new ArrayList<>());
		validate = new Validate(errLog);
		random = new Random();
	}
	
	@AfterEach
	void clear(){
		errLog.clear();
	}
	
	void noErrors(){
		Assertions.assertAll(
				() -> assertFalse(errLog.hasEntries()),
				() -> assertEquals("", errLog.toString())
		);
	}
	
	void assertError(String msg){
		Assertions.assertAll(
				() -> assertTrue(errLog.hasEntries()),
				() -> assertEquals(errLog.toString(), "Errors:\n"+msg)
		);
	}
	
	@Test
	@DisplayName ("static variables are correct")
	void staticVars(){
		Assertions.assertAll(
				() -> assertEquals(512, Validate.MAX_FILE_LINES),
				() -> assertEquals(256, Validate.MAX_INSTR_COUNT),
				() -> assertEquals(4194304, Validate.BASE_INSTR_ADDRESS),
				() -> assertEquals(5242880, Validate.OVER_SUPPORTED_INSTR_ADDRESS),
				() -> assertEquals(268435456, Validate.OVER_INSTR_ADDRESS),
				() -> assertEquals(268500992, Validate.BASE_DATA_ADDRESS),
				() -> assertEquals(268503040, Validate.OVER_SUPPORTED_DATA_ADDRESS),
				() -> assertEquals(268697600, Validate.OVER_DATA_ADDRESS),
				() -> assertEquals(8, Validate.DATA_ALIGN)
		);
	}
	
	@Nested
	@DisplayName ("Validate Address Tests")
	class ValidateAddressTests{
		//------INSTR ADDRESSES--------------
		@ParameterizedTest
		@ValueSource (longs = {0x00400000L, 0x004003E4L, 0x004FFFFCL})
		@DisplayName ("Validate - Supported Instr Address")
		void validateSupportedInstrAddress(long address){
			assertTrue(validate.isSupportedInstrAddr((int) address));
			noErrors();
			assertEquals(Convert.address2Index((int)address),validate.addr2index((int) address));
			noErrors();
		}
		
		@ParameterizedTest
		@ValueSource (longs = {0x00500000L, 0x08000000L, 0x0FFFFFFCL})
		@DisplayName ("Validate - Not_Supported-Valid Instr Address")
		void validateNot_SupportedInstrAddress(long address){
			assertFalse(validate.isSupportedInstrAddr((int) address));
			assertError("\tInstruction Address: \""+Convert.uInt2Hex((int) address)+"\" Not Supported!\n");
		}
		
		@Test
		@DisplayName ("Validate - Invalid Instr Address_Under")
		void validateInvalid_InstrAddressUnder(){
			assertFalse(validate.isSupportedInstrAddr((int) 0x003FFFFCL));
			assertError("\tInstruction Address: \"0x003FFFFC\" Not Valid!\n");
		}
		
		@Test
		@DisplayName ("Validate - Invalid Instr Address_Over")
		void validateInvalid_InstrAddressOver(){
			assertFalse(validate.isSupportedInstrAddr((int) 0x10000000L));
			assertTrue(errLog.hasEntries());
			assertEquals("Errors:\n\tInstruction Address: \"0x10000000\" Not Valid!\n", errLog.toString());
		}
		
		//------DATA ADDRESSES---------------
		@ParameterizedTest
		@ValueSource (longs = {0x10010000L, 0x10010020L, 0x10010800L-8})
		@DisplayName ("Validate - Supported Data Address")
		void validateSupportedDataAddress(long address){
			assertTrue(validate.isSupportedDataAddr((int) address));
			noErrors();
			assertEquals(Convert.address2Index((int)address), validate.addr2index((int) address));
			noErrors();
		}
		
		@ParameterizedTest
		@ValueSource (longs = {0x10010800L, 0x10020000L, 0x10040000L-8})
		@DisplayName ("Validate - Not_Supported-Valid Data Address")
		void validateNot_SupportedDataAddress(long address){
			assertFalse(validate.isSupportedDataAddr((int) address));
			assertTrue(errLog.hasEntries());
			assertEquals("Errors:\n\tData Address: \""
					+Convert.uInt2Hex((int) address)+"\" Not Supported!\n", errLog.toString());
			errLog.clear();
			assertNull(validate.addr2index((int) address));
			assertTrue(errLog.hasEntries());
		}
		
		@Test
		@DisplayName ("Validate - Invalid Data Address_Under")
		void validateInvalid_DataAddressUnder(){
			assertFalse(validate.isSupportedDataAddr( 0x00400000));
			assertTrue(errLog.hasEntries());
			assertEquals("Errors:\n\tData Address: \"0x00400000\" Not Valid!\n", errLog.toString());
			errLog.clear();
			assertNull(validate.addr2index(0x10000000));
			assertTrue(errLog.hasEntries());
		}
		
		@Test
		@DisplayName ("Validate - Invalid Data Address_Over")
		void validateInvalid_DataAddressOver(){
			assertFalse(validate.isSupportedDataAddr((int) 0x7FFFFFFFL));
			assertTrue(errLog.hasEntries());
			assertEquals("Errors:\n\tData Address: \"0x7FFFFFFF\" Not Valid!\n", errLog.toString());
			errLog.clear();
			assertNull(validate.addr2index((int) 0x78000000L));
			assertTrue(errLog.hasEntries());
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
			noErrors();
		}
		
		@ParameterizedTest
		@ValueSource (strings = {".global", ".extern", ".DATA"})
		@DisplayName ("Validate Not Supported Directive")
		void validateNot_SupportedDirective(String directive){
			assertFalse(validate.isValidDirective(10, directive));
			assertError("\tLineNo: 10\tDirective: \""+directive+"\" Not Supported!\n");
		}
		
		//DataTypes .word
		@ParameterizedTest
		@ValueSource (strings = {".word"})
		@DisplayName ("Validate Supported DataType")
		void validateSupportedDataType(String type){
			assertTrue(validate.isValidDataType(5, type));
			noErrors();
		}
		
		@ParameterizedTest
		@ValueSource (strings = {".double", ".half", ".byte", ".float", ".panda", ".WORD", ".wo rd",
				".data", ".text", ".code"})
		@DisplayName ("Validate Not Supported DataType")
		void validateNotSupportedDataType(String type){
			assertFalse(validate.isValidDataType(20, type));
			assertError("\tLineNo: 20\tDataType: \""+type+"\" Not Supported!\n");
		}
		
		//Labels _[a-z] separators ['.','-','_',a-z]*
		@ParameterizedTest
		@ValueSource (strings = {"_label", "label", "also_a.label-with_splits", "label0"})
		@DisplayName ("Validate Supported Labels")
		void validateSupportedLabels(String label){
			assertTrue(validate.isValidLabel(40, label));
			noErrors();
		}
		
		@ParameterizedTest
		@ValueSource (strings = {"_ label with spaces", "73label", "CAPITALIZED", ".data"})
		@DisplayName ("Validate Not Supported Labels")
		void validateNot_SupportedLabels(String label){
			assertFalse(validate.isValidLabel(72, label));
			assertError("\tLineNo: 72\tLabel: \""+label+"\" Not Supported!\n");
		}
		
		//OpCodes add, sub, addi, lw, sw, j, jal, halt, exit
		@ParameterizedTest
		@ValueSource (strings = {"add", "sub", "addi", "lw", "sw", "j", "jal", "halt", "exit"})
		@DisplayName ("Validate Supported Opcodes")
		void validateSupportedOpcodes(String opcode){
			assertTrue(validate.isValidOpCode(1, opcode));
			noErrors();
		}
		
		@ParameterizedTest
		@ValueSource (strings = {"panda", "l.d", "s.d", "la", "lui", ""})
		@DisplayName ("Validate Not Supported Opcodes")
		void validateNot_SupportedOpcodes(String opcode){
			assertFalse(validate.isValidOpCode(230, opcode));
			assertError("\tLineNo: 230\tOpcode: \""+opcode+"\" Not Supported!\n");
		}
	}
	
	@Nested
	@DisplayName ("Validate Operands")
	class Val_Operands{
		DataType type = DataType.NORMAL;
		
		@Test
		@DisplayName ("Test Operands, EXIT")
		void testOperands_EXIT(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(12,"exit",null, warningsLog);
			assertNotNull(operands);
			assertAll(
					() -> assertNull(operands.getLabel()),
					() -> assertNull(operands.getImmediate()),
					() -> assertNull( operands.getRd()),
					() -> assertNull( operands.getRs()),
					() -> assertNull( operands.getRt()),
					() -> assertEquals(Operands.InstrType.R, operands.getInstrType()),
					() -> assertFalse(errLog.hasEntries()),
					() -> assertFalse(warningsLog.hasEntries())
			);
			Operands operands2 = validate.splitValidOperands(20,"halt",null, warningsLog);
			assertNotNull(operands2);
			assertAll(
					() -> assertNull(operands2.getLabel()),
					() -> assertNull(operands2.getImmediate()),
					() -> assertNull(operands2.getRd()),
					() -> assertNull(operands2.getRs()),
					() -> assertNull(operands2.getRt()),
					() -> assertEquals(Operands.InstrType.R, operands2.getInstrType()),
					() -> assertFalse(errLog.hasEntries()),
					() -> assertFalse(warningsLog.hasEntries())
			);
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Exit")
		void testInvalid_Operands_Exit(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(12,"exit","$0, $0, $0", warningsLog);
			assertNull(operands);
			assertAll(
					() -> assertTrue(errLog.hasEntries()),
					() -> assertEquals("Errors:\n\tLineNo: 12\tOperands: [$0, $0, $0]"
							+" for Opcode: \"exit\" Not Valid !\n", errLog.toString()),
					() -> assertFalse(warningsLog.hasEntries())
			);
		}
		//-------------------------------------------------------------------
		// Testing add with good values
		@Test
		@DisplayName ("Test Operands, ADD")
		void testOperands_Add(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(12,"add","$8,r31, $s2", warningsLog);
			assertNotNull(operands);
			
			assertAll(
					() -> assertNull(operands.getLabel()),
					() -> assertNull(operands.getImmediate()),
					() -> assertEquals(Operands.InstrType.R, operands.getInstrType()),
					() -> assertEquals(8, operands.getRd()),
					() -> assertEquals(31, operands.getRs()),
					() -> assertEquals(18, operands.getRt()),
					() -> assertFalse(errLog.hasEntries()),
					() -> assertFalse(warningsLog.hasEntries())
					);
		}
		@Test
		@DisplayName ("Test Operands, SUB")
		void testOperands_Sub(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(30,"sub","$zero, 31, $s2", warningsLog);
				// Second operands "31" should be invalid
			String errors = "Errors:\n\tLineNo: 30\tRegister: \"31\" Not Recognised!\n"
					+"\tLineNo: 30\tOperands: [$zero, 31, $s2] for Opcode: \"sub\" Not Valid !\n";
			String warnings = "Warnings:\n\tLineNo: 30\tDestination Register: \"$zero\" Cannot be modified!,"
					+"\t Result will be ignored!\n";
			
			assertAll(
					() -> assertNull(operands),
					() -> assertTrue(errLog.hasEntries()),
					() -> assertEquals(errors, errLog.toString()),
					() -> assertTrue(warningsLog.hasEntries()),
					() -> assertEquals(warnings, warningsLog.toString())
					);
		}
		
		@Test
		@DisplayName ("Test Invalid Operands, Multiple")
		void testInvalid_Operands_Multiple(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(30,"add","$panda, 31, $ss", warningsLog);
			
			String errors = "Errors:\n\tLineNo: 30	Register: \"$panda\" Not Valid!\n"+
					"\tLineNo: 30	Register: \"31\" Not Recognised!\n"+"\tLineNo: 30	Register: \"$ss\" Not Valid!\n"+
					"\tLineNo: 30\tOperands: [$panda, 31, $ss] for Opcode: \"add\" Not Valid !\n";
			
			assertAll(
					() -> assertNull(operands),
					() -> assertTrue(errLog.hasEntries()),
					() -> assertEquals(errors, errLog.toString()),
					() -> assertFalse(warningsLog.hasEntries())
			);
		}
		
		@Test
		@DisplayName ("Test Operands, Immediate")
		void testOperandsImmediate(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(72,"addi","$1, r20, 75", warningsLog);
			assertNotNull(operands);
			
			assertAll(
					() -> assertNull(operands.getLabel()),
					() -> assertEquals(75, operands.getImmediate()),
					() -> assertEquals(Operands.InstrType.I_write, operands.getInstrType()),
					() -> assertNull(operands.getRd()),
					() -> assertEquals(20, operands.getRs()),
					() -> assertEquals(1, operands.getRt()),
					() -> assertFalse(errLog.hasEntries()),
					() -> assertFalse(warningsLog.hasEntries())
			);
			
			Operands operands2 = validate.splitValidOperands(72,"addi","$1, r20, $s3", warningsLog);
			assertNull(operands2);
			assertTrue(errLog.hasEntries());
			assertEquals("Errors:\n\tLineNo: 72\tImmediate: \"$s3\" Not Valid Integer!\n"+
					"\tLineNo: 72\tOperands: [$1, r20, $s3] for Opcode: \"addi\" Not Valid !\n", errLog.toString());
			Operands operands3 = validate.splitValidOperands(72,"addi","$1, 0x20", warningsLog);
			assertNull(operands3);
		}
		
		@Test
		@DisplayName ("Test Operands, Load")
		void testOperandsLoad(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(0,"lw","$0, 20($1)", warningsLog);
			assertNotNull(operands);
			assertTrue(warningsLog.hasEntries());
			assertFalse(errLog.hasEntries());
			
			warningsLog.clear();
			Operands operands2 = validate.splitValidOperands(5,"lw","$2, panda", warningsLog);
			assertNotNull(operands2);
			assertFalse(warningsLog.hasEntries());
			assertFalse(errLog.hasEntries());
		}
		@Test
		@DisplayName ("Test Operands, Store")
		void testOperandsStore(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(0,"sw","$0, 20($1)", warningsLog);
			assertNotNull(operands);
			
			warningsLog.clear();
			Operands operands2 = validate.splitValidOperands(5,"sw","$2, _panda", warningsLog);
			assertNotNull(operands2);
			assertFalse(warningsLog.hasEntries());
			assertFalse(errLog.hasEntries());
		}
		
		@Test
		@DisplayName ("Test Operands, Base+Offset")
		void testOperandsBaseOffset(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(0,"sw","$0, 20 ($1)", warningsLog);
			assertNotNull(operands);
			assertEquals("", warningsLog.toString());
			assertEquals("", errLog.toString());
			
			operands = validate.splitValidOperands(0,"sw","$0, ($1)", warningsLog);
			assertNotNull(operands);
			assertEquals("", warningsLog.toString());
			assertEquals("", errLog.toString());
		}
		
		@Test
		@DisplayName ("Test Operands, Jump")
		void testOperandsJump(){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			Operands operands = validate.splitValidOperands(0,"j","0x100009", warningsLog);
			assertNotNull(operands);
			Operands operands2 = validate.splitValidOperands(0,"jal","cat", warningsLog);
			assertNotNull(operands2);
			assertFalse(warningsLog.hasEntries());
			assertFalse(errLog.hasEntries());
			
			Operands operands3 = validate.splitValidOperands(0,"j","0x100009, 50", warningsLog);
			assertNull(operands3);
			assertTrue(errLog.hasEntries());
		}
		
		@Test
		@DisplayName ("Test Valid Label Address")
		void testValidLabelAddress(){
			Assertions.assertTrue(util.Val_Operands.isValidLabelOrInt(7,"0x100000",validate, errLog));
			Assertions.assertFalse(util.Val_Operands.isValidLabelOrInt(8,"panda?",validate, errLog));
			assertTrue(errLog.hasEntries());
			assertEquals("Errors:\n\tLineNo: 8\tLabel: \"panda?\" Not Supported!\n", errLog.toString());
			errLog.clear();
			
			Assertions.assertTrue(util.Val_Operands.isValidLabelOrInt(8,"20",validate, errLog));
			
			Assertions.assertTrue(util.Val_Operands.isValidLabelOrInt(9,"2",validate, errLog));
			Assertions.assertFalse(util.Val_Operands.isValidLabelOrInt(9,"2_",validate, errLog));
			errLog.clear();
			
			Assertions.assertTrue(util.Val_Operands.isValidLabelOrInt(9,"_main",validate, errLog));
			assertFalse(errLog.hasEntries());
		}
		
		//validateLoadRegister
		@Test
		@DisplayName ("Validate Convert Register")
		void validateConvertRegister(){
			Integer i = util.Val_Operands.convertRegister(30,"$0",DataType.FLOATING_POINT,errLog);
			assertNull(i);
			assertEquals("Errors:\n\tLineNo: 30\tRegister: \"$0\" Wrong DataType!\n", errLog.toString());
			errLog.clear();
			Integer z = util.Val_Operands.convertRegister(50,"$-40",DataType.NORMAL,errLog);
			assertNull(z);
			assertEquals("Errors:\n\tLineNo: 50\tRegister: \"$-40\" Not In Range!\n", errLog.toString());
			errLog.clear();
			Integer x = util.Val_Operands.convertRegister(20,"$50",DataType.NORMAL,errLog);
			assertNull(x);
			assertEquals("Errors:\n\tLineNo: 20\tRegister: \"$50\" Not In Range!\n", errLog.toString());
		}
		
		//zeroRegister warning
		@ParameterizedTest
		@ValueSource (strings = {"$zero", "zero", "$r0", "r0", "$0"})
		@DisplayName ("Validate Load Register with Zero")
		void validateZeroLoadRegister(String text){
			WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
			//isValidLoadRegister
			Assertions.assertAll(
					() -> Assertions.assertEquals(0,  util.Val_Operands.convertRegister(7,text, type,errLog)),
					() -> assertFalse(errLog.hasEntries()),
					() -> assertFalse(warningsLog.hasEntries()),
					// convert Register should create no warnings, only convert Write register should!
					() -> Assertions.assertEquals(0,  util.Val_Operands.convertWriteRegister(7,text, type,errLog,warningsLog)),
					() -> assertFalse(errLog.hasEntries()),
					() -> assertTrue(warningsLog.hasEntries()),
					() -> assertEquals("Warnings:\n\tLineNo: 7\tDestination Register: \""+text+
							"\" Cannot be modified!,\t Result will be ignored!\n", warningsLog.toString())
			);
		}
	}
}