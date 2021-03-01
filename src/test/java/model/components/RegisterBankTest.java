package model.components;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterBankTest{
	private final Random random = new Random();
	private int[] regs;
	private final ExecutionLog log = new ExecutionLog(new ArrayList<>());
	private RegisterBank rb;
	
	@BeforeEach
	void setUp(){
		regs = new int[32];
		for (int i = 1; i<32; i++) {	// skip index 0. that should always be ==0;
			int r = random.nextInt();
			regs[i]=r;
		}
		rb= new RegisterBank(regs, log);
	}
	
	@AfterEach
	void tearDown(){
		log.clear();
	}
	
	private void expectLog(String msg){
		assertEquals("Execution:\n"+msg, log.toString());
	}
	
	// Construction failure
	@Test
	@DisplayName ("Test Construct With Wrong Size IntArray")
	void testConstructWithWrongSizeIntArray(){
		regs[0] = 50; // regs[0] !=0
		assertThrows(IllegalArgumentException.class, () -> new RegisterBank(regs, log));
		
		regs = new int[30]; // !=32
		assertThrows(IllegalArgumentException.class, () -> new RegisterBank(regs, log));
	}
	
	// READ
	@Test
	@DisplayName ("Test Read - RegisterBank")
	void testReadRegisterBank(){
		for (int i = 0; i<regs.length; i++) {
			assertEquals(regs[i], rb.read(i));
			assertEquals("Execution:\n\tRegisterBank:\tReading Value["+regs[i]
					+"]\tFrom Register Index[R"+i+"]!\n", log.toString());
			log.clear();
		}
	}
	
	@Test
	@DisplayName ("Read Null - RegisterBank")
	void readNullRegisterBank(){
		assertEquals(0, rb.read(null));
		assertEquals("Execution:\n\tRegisterBank:\tNo Action!\n", log.toString());
		log.clear();
		
		int[] temp = rb.read(30, null);
		assertEquals(regs[30], temp[0]);
		assertEquals(0, temp[1]);
		assertEquals("Execution:\n\tRegisterBank:\tReading Value["+regs[30]
				+"]\tFrom Register Index[R30]!\n", log.toString());
	}
	
	@Test
	@DisplayName ("Test DoubleRead - RegisterBank")
	void testDoubleReadRegisterBank(){ // Reading 2 operands
		int[] temp =  rb.read(10, 20);
		assertEquals(regs[10], temp[0]);
		assertEquals(regs[20], temp[1]);
		
		assertEquals("Execution:\n\tRegisterBank:\tReading Values["+regs[10]+", "+regs[20]
				+"]\tFrom Register Indexes[R10, R20]!\n", log.toString());
	}
	
	@Test
	@DisplayName ("Test Read OutOfBounds - RegisterBank")
	void testReadOutOfBoundsRegisterBank(){
		//<0 >31
		assertAll(
				() -> assertThrows(IndexOutOfBoundsException.class, () -> rb.read(-1)),
				() -> assertThrows(IndexOutOfBoundsException.class, () -> rb.read(32)),
				() -> assertThrows(IndexOutOfBoundsException.class, () -> rb.read(64)),
				() -> assertThrows(IndexOutOfBoundsException.class, () -> rb.read(-20)),
				() -> assertThrows(IndexOutOfBoundsException.class, () -> rb.read(0, -1)),
				() -> assertThrows(IndexOutOfBoundsException.class, () -> rb.read(-1, -1))
		);
	}
	
		// store . expect value stored / except 0
	// null -> no action
	
	@Test
	void store(){
		// Validate initial state
		for (int i = 0; i<regs.length; i++) {
			assertEquals(regs[i], rb.read(i));
		}
		log.clear();

		int newVal = random.nextInt();
		rb.write(0, newVal);
		assertEquals(0, regs[0]); // 0 should be unmodified
		assertEquals("Execution:\n\tRegisterBank:\tNo Action!\n", log.toString());
		assertNotEquals(newVal, rb.read(0));
		log.clear();
		
		
		for (int i = 1; i<regs.length; i++) { // skip 0
			log.clear();
			newVal = random.nextInt();
			rb.write(i, newVal);
			assertEquals("Execution:\n\tRegisterBank:\tWriting Value["+newVal
					+"]\tTo Register Index[*R"+i+"]!\n", log.toString());
			assertEquals(newVal, regs[i]);
			assertEquals(newVal, rb.read(i));
		}
	}
	
	//store 0
	@Test
	@DisplayName ("Test Store Zero - Register Bank")
	void testStoreZeroRegisterBank(){
		rb.write(0, 57);
		assertEquals(0, regs[0]);
		assertEquals("Execution:\n\tRegisterBank:\tNo Action!\n", log.toString());
		assertEquals(0, rb.read(0));
	}
	
	@Test
	void format(){
		// Make everything =5 so output is predicable
		String fmt = "";
		fmt += "-----------REGISTER-BANK-------------\n";
		fmt += "|R0: 0\t\tR8: 5\tR16: 5\tR24: 5|\n";
		fmt += "|R1: 5\t\tR9: 5\tR17: 5\tR25: 5|\n";
		fmt += "|R2: 5\t\tR10: 5\tR18: 5\tR26: 5|\n";
		fmt += "|R3: 5\t\tR11: 5\tR19: 5\tR27: 5|\n";
		fmt += "|R4: 5\t\tR12: 5\tR20: 5\tR28: 5|\n";
		fmt += "|R5: 5\t\tR13: 5\tR21: 5\tR29: 5|\n";
		fmt += "|R6: 5\t\tR14: 5\tR22: 5\tR30: 5|\n";
		fmt += "|R7: 5\t\tR15: 5\tR23: 5\tR31: 5|\n";
		fmt += "-------------------------------------";
		for (int i = 1; i<regs.length; i++) {
			regs[i]=5;
		}
		
		assertEquals(fmt, rb.format());
	}
}