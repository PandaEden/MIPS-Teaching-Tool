package model.instr;

import Util.Logs.ErrorLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OperandsTest{
	static final int zero = 0;
	HashMap<String,Integer> labelsMap = new HashMap<>(Map.of("panda", 446789,"x",5));
	ErrorLog errorLog = new ErrorLog(new ArrayList<>());
	
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
				() -> assertThrows(NullPointerException.class, () ->
						operands.setImmediate(errorLog,labelsMap))
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
				() -> assertThrows(NullPointerException.class, () ->
						operands.setImmediate(errorLog,labelsMap))
				);
	}
	
	@Test
	@DisplayName ("Test Operands for AddI")
	void testOperandsForAddI(){
		Operands operands = new Operands(1,5, 6, 478);
		Assertions.assertAll(
				() -> assertEquals(5, operands.getRs()),
				() -> assertEquals(6, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
				() -> assertEquals(478, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("I_write", operands.getInstrType().name()),
				() -> assertThrows(NullPointerException.class, () ->
						operands.setImmediate(errorLog,labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for Load")
	void testOperandsForLoad(){
		Operands operands = new Operands(2,15, 26, -50);
		Assertions.assertAll(
				() -> assertEquals(15, operands.getRs()),
				() -> assertEquals(26, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
				() -> assertEquals(-50, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("I_write", operands.getInstrType().name()),
				() -> assertThrows(NullPointerException.class, () ->
						operands.setImmediate(errorLog,labelsMap))
		);
	}
	
	@Test
	@DisplayName ("Test Operands for Store")
	void testOperandsForStore(){
		Operands operands = new Operands(3,null, 56, 72);
		Assertions.assertAll(
				() -> assertEquals(zero, operands.getRs()),
				() -> assertEquals(56, operands.getRt()),
				() -> assertEquals(zero, operands.getRd()),
				() -> assertEquals(72, operands.getImmediate()),
				() -> assertNull(operands.getLabel()),
				() -> assertEquals("I_read", operands.getInstrType().name()),
				() -> assertThrows(NullPointerException.class, () ->
						operands.setImmediate(errorLog,labelsMap))
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
				() -> assertThrows(NullPointerException.class, () ->
						operands.setImmediate(errorLog,labelsMap))
		);
	}
	
	@Test
	@Disabled
	@DisplayName ("Test Operands for JumpAndLink")
	void testOperandsForJumpAndLink(){
		org.junit.jupiter.api.Assertions.fail("Not implemented");
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
				() -> assertTrue(operands.setImmediate(errorLog,labelsMap)),
				() -> assertEquals(446789, operands.getImmediate())
				);
		Operands operands2 = new Operands(3,0,0,null,"x");
		Assertions.assertAll(
				() -> assertEquals(zero, operands2.getRs()),
				() -> assertEquals(zero, operands2.getRt()),
				() -> assertEquals(zero, operands2.getRd()),
				() -> assertEquals(zero, operands2.getImmediate()),
				() -> assertEquals("x", operands2.getLabel()),
				() -> assertEquals("I_read", operands2.getInstrType().name()),
				() -> assertTrue(operands2.setImmediate(errorLog,labelsMap)),
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
				() -> assertEquals("not a panda",operands.getLabel()),
				() -> assertEquals("J", operands.getInstrType().name()),
				() -> assertFalse(operands.setImmediate(errorLog,labelsMap)),
				() -> assertEquals("Errors:\n\tLabel \"not a panda\" Not Found!", errorLog.toString())
		);
	}
}