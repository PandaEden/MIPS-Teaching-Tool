package model.instr;

import org.junit.jupiter.api.*;
import util.logs.ErrorLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OperandsTest{
	static final int zero = 0;
	HashMap<String, Integer> labelsMap = new HashMap<>(Map.of("panda", 446789, "x", 5));
	ErrorLog errorLog = new ErrorLog(new ArrayList<>());
	
	@AfterEach
	void tearDown(){
		errorLog.clear();
	}
	
	@Test
	@DisplayName ("Test Operands for EXIT")
	void testOperandsForExit(){
		Operands operands = Operands.getExit();
		Assertions.assertAll(
				() -> assertEquals(zero, operands.getRs()),
				() -> assertEquals(zero, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
				() -> assertEquals(zero, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("R", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for Add or Sub")
	void testOperandsForAddOrSub(){
		Operands operands = new Operands(5, 6, 20);
		Assertions.assertAll(
				() -> assertEquals(5, operands.getRs()),
				() -> assertEquals(6, operands.getRt()),
				() -> assertEquals(20, operands.getRd()),
				() -> assertEquals(zero, operands.getImmediate()),
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
				() -> assertEquals(zero, operands.getRd()),
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
				() -> assertEquals(zero, operands.getRd()),
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
				() -> assertEquals(zero, operands.getRs()),
				() -> assertEquals(56, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
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
		Operands operands = new Operands(11892);
		Assertions.assertAll(
				() -> assertEquals(zero, operands.getRs()),
				() -> assertEquals(zero, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
				() -> assertEquals(11892, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertThrows(IllegalArgumentException.class, () ->
						operands.setImmediate(errorLog, labelsMap))
		);
	}
	
	@Test
	@Disabled
	@DisplayName ("Test Operands for JumpAndLink")
	void testOperandsForJumpAndLink(){
		fail("Not implemented");
	}
	
	@Test
	@DisplayName ("Test Operands using Label")
	void testOperandsUsingLabel(){
		Operands operands = new Operands("panda");
		Assertions.assertAll(
				() -> assertEquals(zero, operands.getRs()),
				() -> assertEquals(zero, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
				() -> assertEquals(zero, operands.getImmediate()),
				() -> assertEquals("panda", operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertTrue(operands.setImmediate(errorLog, labelsMap)),
				() -> assertEquals(446789, operands.getImmediate())
		);
		Operands operands2 = new Operands("sw", 0, "x");
		Assertions.assertAll(
				() -> assertEquals(zero, operands2.getRs()),
				() -> assertEquals(zero, operands2.getRt()),
				() -> assertEquals(zero, operands2.getRd()),
				() -> assertEquals(zero, operands2.getImmediate()),
				() -> assertEquals("x", operands2.getLabel()),
				() -> assertEquals("I_read", operands2.getInstrType().name()),
				() -> assertTrue(operands2.setImmediate(errorLog, labelsMap)),
				() -> assertEquals(5, operands2.getImmediate())
		);
	}
	
	@Test
	@DisplayName ("Test Operands Label, label not found")
	void testOperandsForLabelNotFound(){
		Operands operands = new Operands("not a panda");
		assertAll(
				() -> assertEquals(zero, operands.getRs()),
				() -> assertEquals(zero, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
				() -> assertEquals(zero, operands.getImmediate()),
				() -> assertEquals("not a panda", operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertFalse(operands.setImmediate(errorLog, labelsMap)),
				() -> assertEquals("Errors:\n\tLabel \"not a panda\" Not Found!", errorLog.toString())
		);
	}
	
	@Test
	@DisplayName ("Test setImmediate, Invalid Use, Blank label")
	void testSetImmediateInvalidUseNoLabel(){
		labelsMap.put("", -2); //can't possibly be matched as empty/blank labels return IAE
		
		Operands operands = new Operands("   "); // Empty label
		assertThrows(IllegalArgumentException.class, () -> operands.setImmediate(errorLog, labelsMap));
	}
	
	@Test
	@DisplayName ("Test setImmediate, Invalid Address for Type")
	void testSetImmediateInvalidAddress(){
		Operands jump = new Operands("data");
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
				() -> assertEquals("Errors:\n\tInstruction Address\"0x10010000\" Not Valid!\n"+
						"\tLabel points to Invalid Instruction Address\n", errorLog.toString())
		);
		errorLog.clear();
		//Load
		assertAll(
				() -> assertFalse(load.setImmediate(errorLog, labelsMap)),
				() -> assertEquals("Errors:\n\tData Address\"0x00400000\" Not Valid!\n"+
						"\tLabel points to Invalid Data Address\n", errorLog.toString())
		);
		errorLog.clear();
		//Store
		assertAll(
				() -> assertFalse(store.setImmediate(errorLog, labelsMap)),
				() -> assertEquals("Errors:\n\tData Address\"0x00400004\" Not Valid!\n"+
						"\tLabel points to Invalid Data Address\n", errorLog.toString())
		);
		errorLog.clear();
	}
}