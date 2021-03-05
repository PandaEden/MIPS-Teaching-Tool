package model.instr;

import org.junit.jupiter.api.*;
import util.logs.ErrorLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OperandsTest{
	private final HashMap<String, Integer> labelsMap = new HashMap<>(Map.of("panda", 0x400004, "x", 8));
	private final ErrorLog errorLog = new ErrorLog(new ArrayList<>());
	
	@AfterEach
	void tearDown(){
		errorLog.clear();
	}
	
	@Test
	@DisplayName ("Test Operands for EXIT")
	void testOperandsForExit(){
		Operands operands = Operands.getExit();
		Assertions.assertAll(
				() -> assertNull(operands.getRs()),
				() -> assertNull(operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertNull(operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("R", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for R type (ADD/SUB)")
	void testOperandsForRType(){
		Operands operands = new Operands(5, 6, 20);
		Assertions.assertAll(
				() -> assertEquals(5, operands.getRs()),
				() -> assertEquals(6, operands.getRt()),
				() -> assertEquals(20, operands.getRd()),
				() -> assertNull(operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("R", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for AddI")
	void testOperandsForAddI(){
		Operands operands = new Operands("addi", 5, 6, 478);
		Assertions.assertAll(
				() -> assertEquals(5, operands.getRs()),
				() -> assertEquals(6, operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertEquals(478, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("I_write", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for Load")
	void testOperandsForLoad(){
		Operands operands = new Operands("lw", 15, 26, -50);
		Assertions.assertAll(
				() -> assertEquals(15, operands.getRs()),
				() -> assertEquals(26, operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertEquals(-50, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("I_write", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for Store")
	void testOperandsForStore(){
		Operands operands = new Operands("sw", null, 56, 72);
		Assertions.assertAll(
				() -> assertNull(operands.getRs()),
				() -> assertEquals(56, operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertEquals(72, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("I_read", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for Jump")
	void testOperandsForJump(){
		Operands operands = new Operands("j", 11892);
		Assertions.assertAll(
				() -> assertNull(operands.getRs()),
				() -> assertNull(operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertEquals(11892, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for JumpAndLink")
	void testOperandsForJumpAndLink(){
		Operands operands = new Operands("jal", 11892);
		Assertions.assertAll(
				() -> assertNull(operands.getRs()),
				() -> assertNull(operands.getRt()),
				() -> assertEquals(31, operands.getRd()),
				() -> assertEquals(11892, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands using Label")
	void testOperandsUsingLabel(){
		Operands operands = new Operands("j", "panda");
		Assertions.assertAll(
				() -> assertNull(operands.getRs()),
				() -> assertNull(operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertNull(operands.getImmediate()),
				() -> assertEquals("panda", operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertTrue(operands.setImmediate(errorLog, labelsMap)),
				() -> assertNull(operands.getRs()),
				() -> assertEquals(0x400004/4, operands.getImmediate())
		);
		Operands operands2 = new Operands("sw", 0, "x");
		Assertions.assertAll(
				() -> assertNull(operands2.getRs()),
				() -> assertEquals(0, operands2.getRt()),
				() -> assertNull(operands2.getRd()),
				() -> assertNull( operands2.getImmediate()),
				() -> assertEquals("x", operands2.getLabel()),
				() -> assertEquals("I_read", operands2.getInstrType().name()),
				() -> assertFalse(operands2.setImmediate(errorLog, labelsMap)),
				() -> assertNull(operands2.getRs()),
				() -> assertEquals("Errors:\n\tData Address: \"0x00000008\" Not Valid!\n"+
						"\tLabel: \"x\" points to Invalid Data Address!\n", errorLog.toString())
		);
		errorLog.clear();
		Operands operands3 = new Operands("jal", "panda");
		Assertions.assertAll(
				() -> assertNull(operands3.getRs()),
				() -> assertNull(operands3.getRt()),
				() -> assertEquals(31, operands3.getRd()),
				() -> assertNull(operands3.getImmediate()),
				() -> assertEquals("panda", operands3.getLabel()),
				() -> assertEquals("J", operands3.getInstrType().name()),
				() -> assertTrue(operands3.setImmediate(errorLog, labelsMap)),
				() -> assertNull(operands.getRs()),
				() -> assertEquals(0x400004/4, operands3.getImmediate())
		);
		labelsMap.put("__x", 0x10010010);
		Operands operands4 = new Operands("lw", 0, "__x");
		Assertions.assertAll(
				() -> assertNull(operands4.getRs()),
				() -> assertEquals(0, operands4.getRt()),
				() -> assertNull(operands4.getRd()),
				() -> assertNull(operands4.getImmediate()),
				() -> assertEquals("__x", operands4.getLabel()),
				() -> assertEquals("I_write", operands4.getInstrType().name()),
				() -> assertTrue(operands4.setImmediate(errorLog, labelsMap)),
				() -> assertEquals(0, operands4.getRs()),
				() -> assertEquals(0x10010010/4, operands4.getImmediate())
		);
	}
	
	@Test
	@DisplayName ("Test Operands Label, label not found")
	void testOperandsForLabelNotFound(){
		Operands operands = new Operands("j", "not a panda");
		assertAll(
				() -> assertNull(operands.getRs()),
				() -> assertNull(operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertNull(operands.getImmediate()),
				() -> assertEquals("not a panda", operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertFalse(operands.setImmediate(errorLog, labelsMap)),
				() -> assertEquals("Errors:\n\tLabel \"not a panda\" Not Found!\n", errorLog.toString())
		);
	}
	
	@Test
	@DisplayName ("Test setImmediate, Invalid Use, Blank label")
	void testSetImmediateInvalidUseNoLabel(){
		labelsMap.put("", -2); //can't possibly be matched as empty/blank labels return IAE
		
		Operands operands = new Operands("j", "   "); // Empty label
		assertThrows(IllegalArgumentException.class, () -> operands.setImmediate(errorLog, labelsMap));
	}
	
	@Test
	@DisplayName ("Test setImmediate, Null Immediate, Null Label")
	void testSetImmediateInvalidNullImmediate(){
		labelsMap.put("lw", -2); //can't possibly be matched as empty/blank labels return IAE
		
		Operands operands = new Operands("lw",0,0,null); // null Immediate
		assertThrows(IllegalArgumentException.class, () -> operands.setImmediate(errorLog, labelsMap));
	}
	
	@Test
	@DisplayName ("Test setImmediate, Invalid Address for Type")
	void testSetImmediateInvalidAddress(){
		Operands jump = new Operands("j", "data");
		// Branch
		Operands load = new Operands("lw", 56, "ins");
		Operands store = new Operands("sw", 56, "ins2");
		labelsMap.clear();
		
		
		labelsMap.put("data", 0x10010000); // data address
		labelsMap.put("ins", 0x00400000); // instruction address
		labelsMap.put("ins2", 0x00400004); // instruction address
		
		//Jump
		assertAll(
				() -> assertFalse(jump.setImmediate(errorLog, labelsMap)),
				() -> assertEquals("Errors:\n\tInstruction Address: \"0x10010000\" Not Valid!\n"+
				"\tLabel: \"data\" points to Invalid Instruction Address!\n", errorLog.toString())
		);
		errorLog.clear();
		//Load
		assertAll(
				() -> assertFalse(load.setImmediate(errorLog, labelsMap)),
				() -> assertEquals("Errors:\n\tData Address: \"0x00400000\" Not Valid!\n"+
						"\tLabel: \"ins\" points to Invalid Data Address!\n", errorLog.toString())
		);
		errorLog.clear();
		//Store
		assertAll(
				() -> assertFalse(store.setImmediate(errorLog, labelsMap)),
				() -> assertEquals("Errors:\n\tData Address: \"0x00400004\" Not DoubleWord Aligned!\n"+
						"\tLabel: \"ins2\" points to Invalid Data Address!\n", errorLog.toString())
		);
		errorLog.clear();
	}
	
	@Test
	@DisplayName ("Test setImmediate MultipleCalls")
	void testSetImmediateMultipleCalls(){
		Operands operands = new Operands("j", "panda");
		Assertions.assertAll(
				() -> assertNull(operands.getRs()),
				() -> assertNull(operands.getRt()),
				() -> assertNull(operands.getRd()),
				() -> assertNull(operands.getImmediate()),
				() -> assertEquals("panda", operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertTrue(operands.setImmediate(errorLog, labelsMap)),
				() -> assertEquals(0x400004/4, operands.getImmediate()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
}