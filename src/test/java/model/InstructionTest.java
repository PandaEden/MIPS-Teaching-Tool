package model;

import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;
import model.instr.Operands;
import org.junit.jupiter.api.*;
import util.Convert;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class InstructionTest{
	private static final Integer PC = 0x4000C8; // set PC to instr index #51
	private static final ExecutionLog log = new ExecutionLog(new ArrayList<>());
	private static final ErrorLog errors = new ErrorLog(new ArrayList<>());
	private static final HashMap<Integer, Double> data = new HashMap<>();
	private static final HashMap<String, Integer> labelMap = new HashMap<>();
	private static int[] values;
	private RegisterBank rb;
	private DataMemory dm;
	
	@BeforeAll
	static void beforeAll(){
		labelMap.put("instr", InstrMemory.BASE_INSTR_ADDRESS); //Valid INSTR address
		labelMap.put("data", DataMemory.BASE_DATA_ADDRESS); //Valid DATA address  -- index 0
		labelMap.put("inv_instr", InstrMemory.OVER_INSTR_ADDRESS); //Invalid INSTR address
		labelMap.put("inv_data", DataMemory.OVER_DATA_ADDRESS); //Invalid DATA address
	}
	
	@BeforeEach
	void setUp(){
		values = new int[32];
		rb = new RegisterBank(values, log);
		dm = new DataMemory(data, log);
		data.put(Convert.address2Index(labelMap.get("data")), 20.0); // expect value 20, when reading from the valid data address
	}
	
	@AfterEach
	void tearDown(){
		data.clear();
		log.clear();
		errors.clear();
	}
	
	// Test Add
	@Test    // Valid Execution
	@DisplayName ("Test Instruction_ADD")
	void testInstructionAdd(){
		values[6] = 25;
		values[7] = 35;
		assertEquals(0, values[5]); // check initial value is 0
		// Expect after addition, Register # 5 will have the value 60 = (25+35)
		
		Instruction ins = Instruction.buildInstruction("add", new Operands(6, 7, 5));
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(PC+4, newPC);    //NPC = PC+4
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- REGISTER Type Instruction >> \"add\":\n"+
						"\tRegisterBank:\tReading Value[25]\tFrom Register Index[R6]!\n"+
						"\tRegisterBank:\tReading Value[35]\tFrom Register Index[R7]!\n"+
						"\tCalculating Result:\n"+
						"\tRD = RS+RT = 25+35 = 60\n"+
						"\tDataMemory:\tNo Action!\n"+
						"\tRegisterBank:\tWriting Value[60]\tTo Register Index[*R5]!\n",
				log.toString());
		assertEquals(60, rb.read(5));
	}
	
	@Test
	@DisplayName ("Test Invalid Operands Instruction_ADD")
	void testInvalidOperands_InstructionAdd(){
		// Expect after addition, Register # 5 will have the value 60 = (25+35)
		Instruction ins = Instruction.buildInstruction("add", new Operands("x", "data"));
		// Operands for "lw" given to "add"
		
		assertNotNull(ins);
		assertFalse(ins.assemble(errors, labelMap));
		assertThrows(IllegalStateException.class, () -> ins.execute(PC, dm, rb, log));
	}
	
	// Test Sub
	@Test
	@DisplayName ("Test Instruction_SUB")
	void testInstructionSub(){
		values[20] = 250;
		values[24] = 5000;
		assertEquals(0, values[16]); // check initial value is 0
		// Expect after subtraction, Register #16 will have the value -4750 = (5000-78)
		
		Instruction ins = Instruction.buildInstruction("sub", new Operands(20, 24, 16));
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(PC+4, newPC);    //NPC = PC+4
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- REGISTER Type Instruction >> \"sub\":\n"+
						"\tRegisterBank:\tReading Value[250]\tFrom Register Index[R20]!\n"+
						"\tRegisterBank:\tReading Value[5000]\tFrom Register Index[R24]!\n"+
						"\tCalculating Result:\n"+
						"\tRD = RS-RT = 250-5000 = -4750\n"+
						"\tDataMemory:\tNo Action!\n"+
						"\tRegisterBank:\tWriting Value[-4750]\tTo Register Index[*R16]!\n",
				log.toString());
		assertEquals(-4750, rb.read(16));
	}
	
	// Test Addi
	@Test
	@DisplayName ("Test Instruction_ADDI")
	void testInstructionAddI(){
		values[30] = 250;
		values[24] = 5000; // redundant - not used
		assertEquals(0, values[16]); // check initial value is 0
		// Expect after addition, Register #1 will have the value -150 = (250+-400)
		
		Instruction ins = Instruction.buildInstruction("addi", new Operands("addi", 30, 1, -400));
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(PC+4, newPC);    //NPC = PC+4
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- IMMEDIATE Type Instruction >> \"addi\":\n"+
						"\tRegisterBank:\tReading Value[250]\tFrom Register Index[R30]!\n"+
						"\t[IMMEDIATE: -400]\n"+
						"\tCalculating Result:\n"+
						"\tRT = RS+IMMEDIATE = 250+-400 = -150\n"+
						"\tDataMemory:\tNo Action!\n"+
						"\tRegisterBank:\tWriting Value[-150]\tTo Register Index[*R1]!\n",
				log.toString());
		assertEquals(-150, rb.read(1));
	}
	
	// Test LW
	@Test
	@DisplayName ("Test Instruction_LW")
	void testInstructionLW(){
		assertEquals(20, data.get(0)); // check value 20 is at expected location
		assertEquals(0, values[1]); // check initial value is 0
		assertEquals(0x10010000, labelMap.get("data"));
		// Expect after load, Register #1 will have the value 20
		Operands operands = new Operands("lw", 1, "data");
		Instruction ins = Instruction.buildInstruction("lw", operands);
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(PC+4, newPC);    //NPC = PC+4
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- IMMEDIATE Type Instruction >> \"lw\":\n"+
						"\tRegisterBank:\tReading Value[0]\tFrom Register Index[R0]!\n"+
						"\t[IMMEDIATE: 268500992 === 0x10010000]\n"+
						"\tCalculating Address:\n"+
						"\tADDRESS = RS+IMMEDIATE = 0 + 268500992 = 268500992 === 0x10010000\n"+
						"\tDataMemory:\tReading Value[20]\tFrom Memory Address[0x10010000]!\n"+
						"\tRegisterBank:\tWriting Value[20]\tTo Register Index[*R1]!\n",
				log.toString());
		assertEquals(20, rb.read(1));
	}
	
	@Test
	@DisplayName ("Test Instruction LW - BASE+OFFSET")
	void testInstructionLW_BassOffset(){
		values[30] = (DataMemory.BASE_DATA_ADDRESS-40);
		assertEquals(20, data.get(0)); // check value 20 is at expected location
		assertEquals(0, values[1]); // check initial value is 0
		// Expect after load, Register #1 will have the value 20
		
		Instruction ins = Instruction.buildInstruction("lw", new Operands("lw", 30, 1, 40));
		// RS_val + Imm should give the correct address.
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(PC+4, newPC);    //NPC = PC+4
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- IMMEDIATE Type Instruction >> \"lw\":\n"+
						"\tRegisterBank:\tReading Value[268500952]\tFrom Register Index[R30]!\n"+
						"\t[IMMEDIATE: 40 === 0x00000028]\n"+
						"\tCalculating Address:\n"+
						"\tADDRESS = RS+IMMEDIATE = 268500952 + 40 = 268500992 === 0x10010000\n"+
						"\tDataMemory:\tReading Value[20]\tFrom Memory Address[0x10010000]!\n"+
						"\tRegisterBank:\tWriting Value[20]\tTo Register Index[*R1]!\n",
				log.toString());
		assertEquals(20, rb.read(1));
	}
	
	@Test
	@DisplayName ("Test Instruction LW - Invalid to instr")
	void testInstructionLW_Invalid_Instr(){
		Operands operands = new Operands("lw", 1, "instr");
		Instruction ins = Instruction.buildInstruction("lw", operands);
		assertNotNull(ins);
		assertFalse(ins.assemble(errors, labelMap)); // assemble should catch the label points to a instr not data
		
		assertEquals("Errors:\n"+
						"\tData Address: \"0x00400000\" Not Valid!\n"+
						"\tLabel: \"instr\" points to Invalid Data Address!\n",
				errors.toString());
		
		assertThrows(IllegalStateException.class, () -> ins.execute(PC, dm, rb, log));
	}
	
	@Test
	@DisplayName ("Test Instruction LW - Invalid to DataOver")
	void testInstructionLW_Invalid_DataOver(){
		Operands operands = new Operands("lw", 1, "inv_data");
		Instruction ins = Instruction.buildInstruction("lw", operands);
		assertNotNull(ins);
		assertFalse(ins.assemble(errors, labelMap)); // assemble should catch the label points to a instr not data
		
		assertEquals("Errors:\n"+
						"\tData Address: \"0x10040000\" Not Valid!\n"+
						"\tLabel: \"inv_data\" points to Invalid Data Address!\n",
				errors.toString());
		
		assertThrows(IllegalStateException.class, () -> ins.execute(PC, dm, rb, log));
	}
	
	// Test SW
	@Test
	@DisplayName ("Test Instruction_SW")
	void testInstructionSW(){
		assertEquals(20, data.get(0)); // check value 20 is at expected location
		values[30] = 250;
		
		Instruction ins = Instruction.buildInstruction("sw", new Operands("sw", 30, "data"));
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(PC+4, newPC);    //NPC = PC+4
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- IMMEDIATE Type Instruction >> \"sw\":\n"+
						"\tRegisterBank:\tReading Value[0]\tFrom Register Index[R0]!\n"+
						"\tRegisterBank:\tReading Value[250]\tFrom Register Index[R30]!\n"+
						"\t[IMMEDIATE: 268500992 === 0x10010000]\n"+
						"\tCalculating Address:\n"+
						"\tADDRESS = RS+IMMEDIATE = 0 + 268500992 = 268500992 === 0x10010000\n"+
						"\tDataMemory:\tWriting Value[250]\tTo Memory Address[0x10010000]!\n"+
						"\tRegisterBank:\tNo Action!\n",
				log.toString());
		assertEquals(250, data.get(0)); // value at address has changed to expected value
	}
	
	// Test J
	@Test
	@DisplayName ("Test Instruction Jump")
	void testInstructionJump(){
		String label = "instr";
		int address = labelMap.get(label);
		Instruction ins = Instruction.buildInstruction("j", new Operands("j", label));
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(address, newPC);    //NPC = address
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- JUMP Type Instruction >> \"j\":\n"+
						"\tRegisterBank:\tNo Action!\n"+
						"\tDataMemory:\tNo Action!\n"+
						"\tReturning Jump Address: 0x00400000!\n",
				log.toString());
	}
	
	@Test
	@DisplayName ("Test Instruction Jump - Direct")
	void testInstructionJump_Direct(){
		int address = 0x00400014; // index 21
		Instruction ins = Instruction.buildInstruction("j", new Operands("j", address/4));
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(address, newPC);    //NPC = address
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- JUMP Type Instruction >> \"j\":\n"+
						"\tRegisterBank:\tNo Action!\n"+
						"\tDataMemory:\tNo Action!\n"+
						"\tReturning Jump Address: 0x00400014!\n",
				log.toString());
	}
	
	@Test
	@DisplayName ("Test Instruction Jump - Invalid to instrOver")
	void testInstructionJump_Invalid(){
		String label = "inv_instr";
		Instruction ins = Instruction.buildInstruction("j", new Operands("j", label));
		assertNotNull(ins);
		
		assertFalse(ins.assemble(errors, labelMap));
		
		assertEquals("Errors:\n"+
						"\tInstruction Address: \"0x10000000\" Not Valid!\n"+
						"\tLabel: \"inv_instr\" points to Invalid Instruction Address!\n",
				errors.toString());
		assertThrows(IllegalStateException.class, () -> ins.execute(PC, dm, rb, log));
		
	}
	
	@Test
	@DisplayName ("Test Instruction Jump  - Invalid to Data")
	void testInstructionJump_Invalid_Data(){
		String label = "data";
		Instruction ins = Instruction.buildInstruction("j", new Operands("j", label));
		assertNotNull(ins);
		
		assertFalse(ins.assemble(errors, labelMap));
		
		assertEquals("Errors:\n"+
						"\tInstruction Address: \"0x10010000\" Not Valid!\n"+
						"\tLabel: \"data\" points to Invalid Instruction Address!\n",
				errors.toString());
		
		assertThrows(IllegalStateException.class, () -> ins.execute(PC, dm, rb, log));
	}
	
	// Test JAL
	@Test
	@DisplayName ("Test Instruction JumpAndLink")
	void testInstructionJumpAndLink(){
		String label = "instr";
		int address = labelMap.get(label);
		Instruction ins = Instruction.buildInstruction("jal", new Operands("jal", label));
		assertNotNull(ins);
		assertTrue(ins.assemble(errors, labelMap));
		Integer newPC = ins.execute(PC, dm, rb, log);
		assertEquals(address, newPC);    //NPC = address
		
		assertEquals("Execution:\n"+
						"\t\n\t ---- 0x004000C8 ---- JUMP Type Instruction >> \"jal\":\n"+
						"\tStoring Next Program Counter! : 0x004000CC\n"+
						"\tRegisterBank:\tWriting Value[4194508]\tTo Register Index[*R31]!\n"+
						"\tDataMemory:\tNo Action!\n"+
						"\tReturning Jump Address: 0x00400000!\n",
				log.toString());
	}
}