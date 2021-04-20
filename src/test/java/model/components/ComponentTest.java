package model.components;

import _test.TestLogs;
import org.junit.jupiter.api.*;

import model.instr.*;

import util.logs.ExecutionLog;

import static org.junit.jupiter.api.Assertions.*;

class ComponentTest {
	private static TestLogs testLogs;
	private static ExecutionLog log;
	private static ExecutionLog expected;
	@BeforeEach
	void setUp ( ) {
		testLogs = new TestLogs();
		log=testLogs.actualExecution;
		expected=testLogs.expectedExecution;
	}
	@AfterEach
	void tearDown ( ) {
		testLogs.after();
	}
	
	@Nested
	class Multiplexer {
		private final Integer I0=-900;
		private final Integer I1=50;
		
		@Test
		void Input0 ( ) {
			assertEquals( I0, Component.MUX( 0, I0, I1 ) );
		}
		
		@Test
		void Input1 ( ) {
			assertEquals( I1, Component.MUX( 1, I0, I1 ) );
		}
		
		@SuppressWarnings ( "ConstantConditions" ) @Test
		void Input1_Null_PassThrough ( ) {
			assertNull( Component.MUX( null, I0, I1 ));
		}
		
		@Test
		void IndexOutOfBounds ( ) {
			assertThrows( IndexOutOfBoundsException.class, ()-> Component.MUX( 3, I0, I1 ) );
		}
	}
	
	@Nested
	class Adder {
		
		@Test
		void Increment_PC ( ) {
			assertEquals( 121, Component.ADDER( 50, 71, "Incrementing Program Counter", log) );
			expected.appendEx( "Incrementing Program Counter" );
		}
		
		@Test
		void Integer_Overflow ( ) { // Overflow not caught/thrown
			assertEquals( Integer.MIN_VALUE+19 ,Component.ADDER( Integer.MAX_VALUE, 20, "Overflow Not Thrown", log) );
			expected.appendEx( "Overflow Not Thrown" );
		}
		
		@Test
		void Null_Input ( ) {	// Passes though even Null inputs
			assertNull( Component.ADDER( Integer.MAX_VALUE, null, "NotPrinted", log) ); // no Output
		}
	}
	
	@Nested
	class ArithmeticLogicUnit {
		
		@Test
		void Codes ( ) {
			assertAll(
					()-> assertEquals( "NOP", Component.ALU_codes.get( -1 ) ),
					()-> assertEquals( "SLL", Component.ALU_codes.get( 0 ) ),
					()-> assertEquals( "ADD", Component.ALU_codes.get( 2 ) ),
					()-> assertEquals( "SUB", Component.ALU_codes.get( 6 ) ),
					()-> assertEquals( "SLT", Component.ALU_codes.get( 7 ) ),
					()-> assertNull( Component.ALU_codes.get( -2 ) ) // Not actual Value
			);
		}
		
		@Test
		void Search_Code ( ) {
			assertAll(
					()-> assertEquals( -1, Component.searchALUCode( null ) ),
					()-> assertEquals( 0, Component.searchALUCode( "sll" ) ),
					()-> assertEquals( 2, Component.searchALUCode( "add" ) ),
					()-> assertEquals( 6, Component.searchALUCode( "sub" ) ),
					()-> assertEquals( 7, Component.searchALUCode( "slt" ) ),
					()-> assertThrows( IllegalArgumentException.class, ()-> Component.searchALUCode( "panda" ) )
			);
		}
		
		@Test
		void Addition ( ) {
			int ALUResult = Component.ALU( 250, 72, 2, log);
			assertEquals( 322, ALUResult );
			expected.append( "\tResult = 250 + 72 ==> 322" );
		}
		@Test
		@Disabled
		void ADD_Overflow ( ) {
			int ALUResult = Component.ALU( Integer.MAX_VALUE, 1, 2, log);
			assertEquals( Integer.MIN_VALUE, ALUResult ); // needs to set overflow bit
			expected.append( "\tArithmetic Overflow!" );
			fail( "Not implemented" );
		}
		@Test
		void Subtraction ( ) {
			int ALUResult = Component.ALU( 42, 900, 6, log);
			assertEquals( -858, ALUResult );
			expected.append( "\tResult = 42 - 900 ==> -858" );
		}
		@Test
		@Disabled
		void SUB_Underflow ( ) {
			int ALUResult = Component.ALU( Integer.MIN_VALUE, 1, 6, log);
			assertEquals( Integer.MAX_VALUE, ALUResult ); // needs to set overflow bit
			expected.append( "\tArithmetic Underflow" );
			fail( "Not implemented" );
		}
		
		@Test
		void Shift_Left_Logical ( ) {
			int ALUResult = Component.ALU( 0x01Ab, 4, 0, log);
			assertEquals( 0x01Ab0, ALUResult );	// SLL 4 === *2^4 == *16
			expected.append( "\tResult = 427 << 4 ==> 6832" );
		}
		@Test
		void SLL_Negative_Amount ( ) {
			int ALUResult = Component.ALU( 52, -20, 0, log);
			// only last 5 bits are considered, '01100' ==> 12
			assertEquals( 212992, ALUResult );	// SLL 12 === *2^12 == *4096
			expected.append( "\tResult = 52 << 12 ==> 212992" );
		}
		@Test
		void SLL_Above_31 ( ) {
			int ALUResult = Component.ALU( 72, 500, 0, log);
			// only last 5 bits are considered, '10100' ==> 20
			assertEquals( 75497472, ALUResult );	// SLL 20 === *2^20 == *1048576
			expected.append( "\tResult = 72 << 20 ==> 75497472" );
		}
		
		@Test
		void Set_On_Less_Than ( ) {
			int ALUResult = Component.ALU( 5, 20, 7, log);
			assertEquals( 1, ALUResult );
			expected.append( "\tResult = 5 set-on < 20 ==> 1" );
		}
		@Test
		void Set_On_Less_Than_Negative ( ) {
			int ALUResult = Component.ALU( Integer.MIN_VALUE, -1, 7, log);
			assertEquals( 1, ALUResult );
			expected.append( "\tResult = -2147483648 set-on < -1 ==> 1" );
		}
		@Test
		void SLT_GreaterThanEquals ( ) {
			int ALUResult = Component.ALU( 500, 20, 7, log);
			assertEquals( 0, ALUResult );
			expected.append( "\tResult = 500 set-on < 20 ==> 0" );
		}
		
		@Test
		void NOP ( ) { // Pass through Input0  --// No Output
			int ALUResult = Component.ALU( 1, 0, -1, log);
			assertEquals( 1, ALUResult );
		}
		
		@Test
		void Null ( ) { // Pass through Input0  --// No Output
			int ALUResult = Component.ALU( 50, 0, null, log);
			assertEquals( 50, ALUResult );
		}
	}
	
	@Nested
	class Decode {	// Should this be merged with Instruction Tests ?
		
		private void arraysEqual(Integer[] arr1, Integer[] arr2){
			for ( int i=0; i<arr1.length; i++ ) {
				assertEquals(arr1[i],arr2[i] );
			}
		}
		
		@Test
		void R_Add ( ) {
			Integer[] ctrl = Component.DECODE( new R_Type( "add", 1,1,1 ), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[RD]\tALU:ALUSrc1[AIR1], ALUSrc2[AIR2], ALUOp[ADD]" );
			expected.append( "\t\tMemoryBank:Memory[-], MemToReg[No:AOR], PC:PCWrite[NPC]" );
			arraysEqual(new Integer[]{1, 0,0,2 ,null,0, 0}, ctrl);
		}
		
		@Test
		void R_Sub ( ) {
			Integer[] ctrl = Component.DECODE( new R_Type( "sub", 1,1,1 ), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[RD]\tALU:ALUSrc1[AIR1], ALUSrc2[AIR2], ALUOp[SUB]" );
			expected.append( "\t\tMemoryBank:Memory[-], MemToReg[No:AOR], PC:PCWrite[NPC]" );
			arraysEqual(new Integer[]{1, 0,0,6, null,0, 0}, ctrl);
		}
		
		@Test
		void I_Addi ( ) {
			Integer[] ctrl = Component.DECODE( new I_Type( "addi", 1, 1, 20 ), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[RT]\tALU:ALUSrc1[AIR1], ALUSrc2[IMM], ALUOp[ADD]" );
			expected.append( "\t\tMemoryBank:Memory[-], MemToReg[No:AOR], PC:PCWrite[NPC]" );
			arraysEqual(new Integer[]{0, 0,1,2, null,0, 0}, ctrl);
		}
		
		@Test
		void Mem_Lw ( ) {
			Integer[] ctrl = Component.DECODE( new MemAccess( "lw", 2, "panda"), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[RT]\tALU:ALUSrc1[AIR1], ALUSrc2[IMM], ALUOp[ADD]" );
			expected.append( "\t\tMemoryBank:Memory[READ->LMDR], MemToReg[Yes:LMDR], PC:PCWrite[NPC]" );
			arraysEqual(new Integer[]{0, 0,1,2, 0,1, 0}, ctrl);
		}
		
		@Test
		void Mem_Sw ( ) {
			Integer[] ctrl = Component.DECODE( new MemAccess( "sw", 2, "panda"), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[-]\tALU:ALUSrc1[AIR1], ALUSrc2[IMM], ALUOp[ADD]" );
			expected.append( "\t\tMemoryBank:Memory[WRITE<-SVR], MemToReg[-], PC:PCWrite[NPC]" );
			arraysEqual(new Integer[]{null, 0,1,2, 1,null, 0}, ctrl);
		}
		
		@Test
		void J_J ( ) {
			Integer[] ctrl = Component.DECODE( new J_Type( "j", "panda"), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[-]\tALU:ALUSrc1[-], ALUSrc2[-], ALUOp[-]" );
			expected.append( "\t\tMemoryBank:Memory[-], MemToReg[-], PC:PCWrite[IMM]" );
			arraysEqual(new Integer[]{null, null,null,null,null,null, 1}, ctrl);
		}
		
		@Test
		void J_Jal ( ) {
			Integer[] ctrl = Component.DECODE( new J_Type( "jal", "panda"), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[$ReturnAddress:31]\tALU:ALUSrc1[NPC], ALUSrc2[-], ALUOp[NOP]" );
			expected.append( "\t\tMemoryBank:Memory[-], MemToReg[No:AOR], PC:PCWrite[IMM]" );
			arraysEqual(new Integer[]{2, 1,null,-1 ,null,0, 1}, ctrl);
		}
		
		@Test
		void Nop_Exit ( ) {
			Integer[] ctrl = Component.DECODE( new Nop( "exit"), log );
			expected.append( "Decoded Control_Signals:\tRegisterBank:Destination[-]\tALU:ALUSrc1[-], ALUSrc2[-], ALUOp[-]" );
			expected.append( "\t\tMemoryBank:Memory[-], MemToReg[-], PC:PCWrite[-]" );
			arraysEqual(new Integer[]{null, null,null,null ,null,null, null}, ctrl);
		}
	}
}
