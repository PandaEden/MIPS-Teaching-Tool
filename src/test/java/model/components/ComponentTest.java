package model.components;

import _test.TestLogs;
import org.junit.jupiter.api.*;

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
		
		private Integer mux (boolean signal) {
			return Component.Mux( I0, I1, signal );
		}
		
		@Test
		void Input0 ( ) {
			assertEquals( I0, mux( true ) );
		}
		
		@Test
		void Input1 ( ) {
			assertEquals( I1, mux( false ) );
		}
		
		@SuppressWarnings ( "ConstantConditions" ) @Test
		void Input1_Null_PassThrough ( ) {
			assertNull( Component.Mux( I0, null, false ) );
		}
	}
	
	@Nested
	class Adder {
		
		@Test
		void Increment_PC ( ) {
			assertEquals( 121, Component.Adder(50, 71,"Incrementing Program Counter", log) );
			expected.appendEx( "Incrementing Program Counter" );
		}
		
		@Test
		void Integer_Overflow ( ) { // Overflow not caught/thrown
			assertEquals( Integer.MIN_VALUE+19 ,Component.Adder( Integer.MAX_VALUE, 20,"Overflow Not Thrown", log) );
			expected.appendEx( "Overflow Not Thrown" );
		}
		
		@Test
		void Null_Input ( ) {	// Passes though even Null inputs
			assertNull( Component.Adder( Integer.MAX_VALUE, null,"NotPrinted", log) ); // no Output
		}
	}
	
	@Nested
	class ArithmeticLogicUnit {
		@Test
		void Addition ( ) {
			int ALUResult = Component.ALU( 250, 72, "ADD", log);
			assertEquals( 322, ALUResult );
			expected.append( "\tResult = 250 + 72 ==> 322" );
		}
		@Test
		@Disabled
		void ADD_Overflow ( ) {
			int ALUResult = Component.ALU( Integer.MAX_VALUE, 1, "ADD", log);
			assertEquals( Integer.MIN_VALUE, ALUResult ); // needs to set overflow bit
			expected.append( "\tArithmetic Overflow!" );
			fail( "Not implemented" );
		}
		@Test
		void Subtraction ( ) {
			int ALUResult = Component.ALU( 42, 900, "SUB", log);
			assertEquals( -858, ALUResult );
			expected.append( "\tResult = 42 - 900 ==> -858" );
		}
		@Test
		@Disabled
		void SUB_Underflow ( ) {
			int ALUResult = Component.ALU( Integer.MIN_VALUE, 1, "SUB", log);
			assertEquals( Integer.MAX_VALUE, ALUResult ); // needs to set overflow bit
			expected.append( "\tArithmetic Underflow" );
			fail( "Not implemented" );
		}
		
		@Test
		void Shift_Left_Logical ( ) {
			int ALUResult = Component.ALU( 0x01Ab, 4, "SLL", log);
			assertEquals( 0x01Ab0, ALUResult );	// SLL 4 === *2^4 == *16
			expected.append( "\tResult = 427 << 4 ==> 6832" );
		}
		@Test
		void SLL_Negative_Amount ( ) {
			int ALUResult = Component.ALU( 52, -20, "SLL", log);
			// only last 5 bits are considered, '01100' ==> 12
			assertEquals( 212992, ALUResult );	// SLL 12 === *2^12 == *4096
			expected.append( "\tResult = 52 << 12 ==> 212992" );
		}
		@Test
		void SLL_Above_31 ( ) {
			int ALUResult = Component.ALU( 72, 500, "SLL", log);
			// only last 5 bits are considered, '10100' ==> 20
			assertEquals( 75497472, ALUResult );	// SLL 20 === *2^20 == *1048576
			expected.append( "\tResult = 72 << 20 ==> 75497472" );
		}
		
		@Test
		void Set_On_Less_Than ( ) {
			int ALUResult = Component.ALU( 5, 20, "SLT", log);
			assertEquals( 1, ALUResult );
			expected.append( "\tResult = 5 set-on < 20 ==> 1" );
		}
		@Test
		void Set_On_Less_Than_Negative ( ) {
			int ALUResult = Component.ALU( Integer.MIN_VALUE, -1, "SLT", log);
			assertEquals( 1, ALUResult );
			expected.append( "\tResult = -2147483648 set-on < -1 ==> 1" );
		}
		@Test
		void SLT_GreaterThanEquals ( ) {
			int ALUResult = Component.ALU( 500, 20, "SLT", log);
			assertEquals( 0, ALUResult );
			expected.append( "\tResult = 500 set-on < 20 ==> 0" );
		}
		
		@Test
		void NOP ( ) { // Pass through Input0  --// No Output
			int ALUResult = Component.ALU( 1, 0, "NOP", log);
			assertEquals( 1, ALUResult );
		}
	}
	
}
