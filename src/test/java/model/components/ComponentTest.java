package model.components;

import _test.Tags;
import _test.TestLogs;
import _test.TestLogs.FMT_MSG._Execution;
import org.junit.jupiter.api.*;

import model.instr.*;

import util.logs.ExecutionLog;

import static org.junit.jupiter.api.Assertions.*;

@Tag( Tags.EX )
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
			assertEquals( I0, Component.MUX( 0, "I0",I0, I1 ) );
		}
		
		@Test
		void Input1 ( ) {
			assertEquals( I1, Component.MUX( 1,"I1", I0, I1 ) );
		}
		
		@SuppressWarnings ( "ConstantConditions" ) @Test
		void Input1_Null_PassThrough ( ) {
			assertNull( Component.MUX( null,"NullPass", I0, I1 ));
		}
		
		@Test
		void IndexOutOfBounds ( ) {
			assertThrows( IndexOutOfBoundsException.class, ()-> Component.MUX( 3,"OutOfBounds", I0, I1 ) );
		}
	}
	
	@Nested
	class Adder {
		
		@Test
		void Increment_PC ( ) {
			assertEquals( 121, Component.ADDER( 50, 71, "Incrementing Program Counter", log) );
			expected.append( "Incrementing Program Counter" );
		}
		
		@Test
		void Integer_Overflow ( ) { // Overflow not caught/thrown
			assertEquals( Integer.MIN_VALUE+19 ,Component.ADDER( Integer.MAX_VALUE, 20, "Overflow Not Thrown", log) );
			expected.append( "Overflow Not Thrown" );
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
					()-> assertEquals( "SLL", Component.ALU_codes.get( 1 ) ),
					()-> assertEquals( "ADD", Component.ALU_codes.get( 0 ) ),
					()-> assertEquals( "SUB", Component.ALU_codes.get( 2 ) ),
					()-> assertEquals( "SLT", Component.ALU_codes.get( 8 ) ),
					()-> assertNull( Component.ALU_codes.get( -2 ) ) // Not actual Value
			);
		}
		
		@Test
		void Search_Code ( ) {
			assertAll(
					()-> assertEquals( -1, Component.searchALUCode( null ) ),
					()-> assertEquals( 1, Component.searchALUCode( "sll" ) ),
					()-> assertEquals( 0, Component.searchALUCode( "add" ) ),
					()-> assertEquals( 2, Component.searchALUCode( "sub" ) ),
					()-> assertEquals( 4, Component.searchALUCode( "and" ) ),
					()-> assertEquals( 5, Component.searchALUCode( "or" ) ),
					()-> assertEquals( 6, Component.searchALUCode( "xor" ) ),
					()-> assertEquals( 8, Component.searchALUCode( "slt" ) ),
					()-> assertEquals( 9, Component.searchALUCode( "slt|e" ) ),
					()-> assertThrows( IllegalArgumentException.class, ()-> Component.searchALUCode( "panda" ) )
			);
		}
		
		@Nested
		class ARITHMETIC {
			
			@Test
			void Addition ( ) {
				int ALUResult = Component.ALU( 250, 72, 0, log);
				assertEquals( 322, ALUResult );
				expected.append( "\tALU Result = 250 + 72 ==> 322" );
			}
			@Test
			@Disabled
			void ADD_Overflow ( ) {
				int ALUResult = Component.ALU( Integer.MAX_VALUE, 1, 0, log);
				assertEquals( Integer.MIN_VALUE, ALUResult ); // needs to set overflow bit
				expected.append( "\tArithmetic Overflow!" );
				fail( "Not implemented" );
			}
			@Test
			void Subtraction ( ) {
				int ALUResult = Component.ALU( 42, 900, 2, log);
				assertEquals( -858, ALUResult );
				expected.append( "\tALU Result = 42 - 900 ==> -858" );
			}
			@Test
			@Disabled
			void SUB_Underflow ( ) {
				int ALUResult = Component.ALU( Integer.MIN_VALUE, 1, 2, log);
				assertEquals( Integer.MAX_VALUE, ALUResult ); // needs to set overflow bit
				expected.append( "\tArithmetic Underflow" );
				fail( "Not implemented" );
			}
			
			@Test
			void Shift_Left_Logical ( ) {
				int ALUResult = Component.ALU( 0x01Ab, 4, 1, log);
				assertEquals( 0x01Ab0, ALUResult );	// SLL 4 === *2^4 == *16
				expected.append( "\tALU Result = 427 << 4 ==> 6832" );
			}
			@Test
			void SLL_Negative_Amount ( ) {
				int ALUResult = Component.ALU( 52, -20, 1, log);
				// only last 5 bits are considered, '01100' ==> 12
				assertEquals( 212992, ALUResult );	// SLL 12 === *2^12 == *4096
				expected.append( "\tALU Result = 52 << 12 ==> 212992" );
			}
			@Test
			void SLL_Above_31 ( ) {
				int ALUResult = Component.ALU( 72, 500, 1, log);
				// only last 5 bits are considered, '10100' ==> 20
				assertEquals( 75497472, ALUResult );	// SLL 20 === *2^20 == *1048576
				expected.append( "\tALU Result = 72 << 20 ==> 75497472" );
			}
			
		}
		
		@Nested
		class Condition {
			
			@Test
			void Set_On_Less_Than ( ) {
				int ALUResult = Component.ALU( 5, 20, 8, log);
				assertEquals( 1, ALUResult );
				expected.append( "\tALU Result = 5 set-on < 20 ==> 1" );
			}
			@Test
			void Set_On_Less_Than_Negative ( ) {
				int ALUResult = Component.ALU( Integer.MIN_VALUE, -1, 8, log);
				assertEquals( 1, ALUResult );
				expected.append( "\tALU Result = -2147483648 set-on < -1 ==> 1" );
			}
			@Test
			void SLT_GreaterThanEquals ( ) {
				int ALUResult = Component.ALU( 500, 20, 8, log);
				assertEquals( 0, ALUResult );
				expected.append( "\tALU Result = 500 set-on < 20 ==> 0" );
			}
			
			@Test
			void Set_On_Less_Than_Equals ( ) {
				int ALUResult = Component.ALU( 5, 5, 9, log);
				assertEquals( 1, ALUResult );
				expected.append( "\tALU Result = 5 set-on <= 5 ==> 1" );
			}
		}
		
		@Nested
		class Logical {
			
			@Test
			void AND ( ) {
				int ALUResult = Component.ALU( 8, 2, 4, log);
				assertEquals( 0, ALUResult );
				expected.append( "\t (binary) '1000' and '10' ==> '0'" );
				expected.append( "\tALU Result = 8 & 2 ==> 0" );
			}
			@Test
			void OR ( ) {
				int ALUResult = Component.ALU( 8, 2, 5, log);
				assertEquals( 10, ALUResult );
				expected.append( "\t (binary) '1000' or '10' ==> '1010'" );
				expected.append( "\tALU Result = 8 | 2 ==> 10" );
			}
			
			@Test
			void XOR ( ) {
				int ALUResult = Component.ALU( 10, 2, 6, log);
				assertEquals( 8, ALUResult );
				expected.append( "\t (binary) '1010' xor '10' ==> '1000'" );
				expected.append( "\tALU Result = 10 ^ 2 ==> 8" );
			}
			
			@Test
			void Equals ( ) {
				int ALUResult = Component.ALU( 10, 10, 6, log);
				assertEquals( 0, ALUResult );
				expected.append( "\t (binary) '1010' xor '1010' ==> '0'" );
				expected.append( "\tALU Result = 10 ^ 10 ==> 0" );
			}
		}
		
		@Test
		void NOP ( ) { // Pass through Input0  --// No Output
			int ALUResult = Component.ALU( 1, 0, -1, log);
			assertEquals( 1, ALUResult );
			expected.append( "\tALU Result = 1 ==> 1" );
		}
		
		@Test
		void NullCode ( ) { // Pass through Input0  --// No Output
			int ALUResult = Component.ALU( 50, 0, null, log);
			assertEquals( 50, ALUResult );
			expected.append( "\tALU Result = 50 ==> 50" );
		}
		
		@Test
		void NullInputs_forNonNOP ( ) { // Pass through Input0  --// No Output
			assertThrows( IllegalArgumentException.class, ()-> Component.ALU( null, 0, 2, log));
		}
		
		@Test
		void NullInputs_forNOP_PassThrough ( ) { // Pass through Input0  --// No Output
			assertNull( Component.ALU( null, 0, null, log));
		}
	}
	
	@Nested
	class Decode {	// Should this be merged with Instruction Tests ?
		
		private void arraysEqual(Integer[] arr1, Integer[] arr2){
			for ( int i=0; i<arr1.length; i++ ) {
				assertEquals(arr1[i],arr2[i] );
			}
		}
		@Nested
		class Register {
			
			@Test
			void R_Add ( ) {
				Integer[] ctrl = Component.DECODER( new R_Type( "add", 1, 1, 1 ), log );
				expected.append( _Execution._control_RType( "add", 1, 1, 1, "ADD"));
				arraysEqual(new Integer[]{1, 0,0,0 ,null,0, 0, null}, ctrl);
			}
			
			@Test
			void R_Sub ( ) {
				Integer[] ctrl = Component.DECODER( new R_Type( "sub", 1, 1, 1 ), log );
				expected.append( _Execution._control_RType( "sub", 1, 1, 1, "SUB"));
				arraysEqual(new Integer[]{1, 0,0,2, null,0, 0, null}, ctrl);
			}
			
		}
		
		@Nested
		class IMM {
			
			@Test
			void I_Addi ( ) {
				Integer[] ctrl = Component.DECODER( new I_Type( "addi", 1, 1, 20 ), log );
				expected.append( _Execution._control_IType("addi",1, 1, 20,"ADD"));
				arraysEqual(new Integer[]{0, 0,1,0, null,0, 0, null}, ctrl);
			}
			
			@Test
			void Mem_Lw ( ) {
				Integer[] ctrl = Component.DECODER( new MemAccess( "lw", 2, "panda"), log );
				expected.append( _Execution._control_Load( 0, 2, null ));
				arraysEqual(new Integer[]{0, 0,1,0, 0,1, 0, null}, ctrl);
			}
			
			@Test
			void Mem_Sw ( ) {
				Integer[] ctrl = Component.DECODER( new MemAccess( "sw", 2, "panda"), log );
				expected.append( _Execution._control_Store( 0, 2, null ));
				arraysEqual(new Integer[]{null, 0,1,0, 1,null, 0, null}, ctrl);
			}
			
			@Nested
			class Branches {
				
				@Test
				void Beq ( ) {
					Integer[] ctrl = Component.DECODER( new Branch( "beq", 1, 1, 1 ), log );
					expected.append( _Execution._control_Branch("beq",1, 1, 1,0,"XOR"));
					arraysEqual(new Integer[]{null, 0,0,6, null,null, 2, 0}, ctrl);
				}
				@Test
				void Bne ( ) {
					Integer[] ctrl = Component.DECODER( new Branch( "bne", 1, 1, 1 ), log );
					expected.append( _Execution._control_Branch("bne",1, 1, 1,1,"XOR"));
					arraysEqual(new Integer[]{null, 0,0,6, null,null, 2, 1}, ctrl);
				}
				
				@Test
				void Blt ( ) {
					Integer[] ctrl = Component.DECODER( new Branch( "blt", 1, 1, 1 ), log );
					expected.append( _Execution._control_Branch("blt",1, 1, 1,1,"SLT"));
					arraysEqual(new Integer[]{null, 0,0,8, null,null, 2, 1}, ctrl);
				}
				@Test
				void Bge ( ) {
					Integer[] ctrl = Component.DECODER( new Branch( "bge", 1, 1, 1 ), log );
					expected.append( _Execution._control_Branch("bge",1, 1, 1,0,"SLT"));
					arraysEqual(new Integer[]{null, 0,0,8, null,null, 2, 0}, ctrl);
				}
				
				@Test
				void Ble ( ) {
					Integer[] ctrl = Component.DECODER( new Branch( "ble", 1, 1, 1 ), log );
					expected.append( _Execution._control_Branch("ble",1, 1, 1,1,"SLT|E"));
					arraysEqual(new Integer[]{null, 0,0,9, null,null, 2, 1}, ctrl);
				}
				@Test
				void Bgt ( ) {
					Integer[] ctrl = Component.DECODER( new Branch( "bgt", 1, 1, 1 ), log );
					expected.append( _Execution._control_Branch("bgt",1, 1, 1,0,"SLT|E"));
					arraysEqual(new Integer[]{null, 0,0,9, null,null, 2, 0}, ctrl);
				}
				
			}
		
		}
		
		@Nested
		class Jumps {
			
			@Test
			void J_J ( ) {
				Integer[] ctrl = Component.DECODER( new J_Type( "j", "panda"), log );
				expected.append( _Execution._control_Jump(null));
				arraysEqual(new Integer[]{null, null,null,null,null,null, 1, null}, ctrl);
			}
			
			@Test
			void J_Jal ( ) {
				Integer[] ctrl = Component.DECODER( new J_Type( "jal", "panda"), log );
				expected.append( _Execution._control_JumpAndLink(null));
				arraysEqual(new Integer[]{2, 1,null,-1 ,null,0, 1, null}, ctrl);
			}
			
		}
		
		@Test
		void Nop_Exit ( ) {
			Integer[] ctrl = Component.DECODER( new Nop( "exit"), log );
			expected.append( _Execution._control_Nop("exit", "-"));
			arraysEqual(new Integer[]{null, null,null,null ,null,null, null, null}, ctrl);
		}
	}
}
