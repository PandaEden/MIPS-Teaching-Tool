package model.instr;

import _test.Tags;
import _test.TestLogs;
import _test.TestLogs.FMT_MSG;
import _test.providers.InstrProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import model.components.DataMemory;
import model.components.InstrMemory;

import util.Convert;
import util.logs.ErrorLog;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@Tag ( Tags.Pkg.MOD )
@Tag ( Tags.INSTR )
@Tag ( Tags.EX )
@Tag ( Tags.OUT )
@DisplayName ( Tags.Pkg.MOD + " : " +Tags.INSTR + " : " + Tags.EX + " Test " )
class InstructionTest {
	private static final int base_PC=InstrMemory.BASE_INSTR_ADDRESS;
	private static final HashMap<Integer, Double> data=new HashMap<>( );
	private static final HashMap<String, Integer> labelMap=InstrProvider.labelsMap;
	private static final int[] values=new int[ 32 ];
	private static TestLogs testLogs;
	private static FMT_MSG._Execution testLogs_ex;
	private static ErrorLog errors;
	
	@BeforeAll
	static void beforeAll ( ) {
		testLogs=new TestLogs( );
		testLogs_ex=new FMT_MSG._Execution( values, data, testLogs.actualExecution, testLogs.expectedExecution );
		errors=testLogs.actualErrors;
		data.clear( );
	}
	
	@AfterEach
	void tearDown ( ) {
		testLogs.after( );
	}
	
	@ParameterizedTest ( name="[{index}] EXIT_Execution : {0}" )
	@ArgumentsSource(InstrProvider.NO_OPS.class)
	void Exit_Execution (String opcode) {
		// Build
		Instruction ins=new Nop( opcode);
		// Execution
		Instr.assembleAndExecute_newPC(null, ins );
		// Output
		testLogs_ex.exit_output( base_PC, opcode );
	}
	
	@Test
	void Undefined_Instruction_ThrowsException_At_Runtime ( ) {
		assertThrows(IllegalArgumentException.class, ()-> new R_Type( "panda", 0, 0, 0 ));
	}
	
	@Nested
	class ARITHMETIC {
		@Test
		void ADD_Execution ( ) {
			//Setup
			values[ 6 ]=25;
			values[ 7 ]=35;
			values[ 5 ]=0;
			// - Expect after addition, Register # 5 will have the value 60 = (25+35)
			// Build
			Instruction ins=new R_Type( "add",6, 7, 5 );
			//Execution
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( 60, values[ 5 ] );
			// Output
			testLogs_ex.R_output( base_PC, "add", 6, 25, 7, 35, 5, 60,"+");
		}
		
		@Test
		void SUB_Execution ( ) {
			// Setup
			values[ 20 ]=250;
			values[ 24 ]=5000;
			assertEquals( 0, values[ 16 ] ); // check initial value is 0
			// - Expect after subtraction, Register #16 will have the value -4750 = (5000-78)
			// Build
			Instruction ins=new R_Type("sub",20, 24, 16 );
			// Execution
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( -4750, values[ 16 ] );
			// Output
			testLogs_ex.R_output( base_PC, "sub", 20, 250, 24, 5000, 16, -4750,"-");
			
		}
		
		@Test
		void Invalid_ADD_Execution_With_Invalid_Operands ( ) {
			// Expect after addition, Register # 5 will have the value 60 = (25+35)
			assertThrows( IllegalArgumentException.class, ()-> new R_Type( "add", 0, 0, -5));
		}
		
	}
	
	@Nested
	class IMMEDIATE_ARITHMETIC {
		
		@Test
		void ADDI_Execution ( ) {
			// Setup
			values[ 30 ]=250;
			values[ 16 ]=0;
			// - Expect after addition, Register #1 will have the value -150 = (250+-400)
			// Build
			Instruction ins=new I_Type( "addi", 30, 1, -400 );
			assertNotNull( ins );
			// Execution
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( -150, values[ 1 ] );
			// Output
			testLogs_ex.I_output( base_PC, "addi", 30, 250, 1, -150, -400,"+");
		}
	}
	
	@Nested
	class IMMEDIATE_MEMORY {
		
		@Test
		void LW_Execution ( ) {
			// Setup
			int addr=labelMap.get( "data" );
			values[ 1 ]=0;
			data.put( Convert.dataAddr2Index( labelMap.get( "data" ) ), 20.0 );
			// - Expect after load, Register #1 will have the value 20
			// Build
			Instruction ins=new MemAccess( "lw", 1, "data" );
			assertNotNull( ins );
			// Execution
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( 20, values[ 1 ] );
			// Output
			testLogs_ex.load_output( base_PC, 0, 0, addr, 1, 20);
		}
		
		@Test
		void LW_Execution_BassOffset ( ) {
			// Setup
			int imm=40;
			int addr=DataMemory.BASE_DATA_ADDRESS;
			values[ 30 ]=(addr - imm);
			values[ 1 ]=0;
			data.put( 0, 200.0 );
			// - Expect after load, Register #1 will have the value 200
			// Build
			Instruction ins=new MemAccess( "lw", 30, 1, imm );
			// RS_val + Imm should give the correct address.
			assertNotNull( ins );
			// Execution
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( 200, values[ 1 ] );
			// Output
			testLogs_ex.load_output( base_PC, 30, values[ 30 ], imm, 1, 200);
			
		}
		
		@Test
		void SW_Execution ( ) {
			// Setup
			int addr=labelMap.get( "data" );
			values[ 30 ]=250;
			data.put( 0, 20.0 );
			// - Expect after store, Memory #0 will have the value 250
			// Build
			Instruction ins=new MemAccess( "sw", 30, "data" );
			assertNotNull( ins );
			// Execution
			Instr.assembleAndExecute_incPC( ins );
			// Results
			assertEquals( 250, data.get( 0 ) ); // value at address has changed to expected value
			// Output
			testLogs_ex.store_output( base_PC, 0, 0, addr, 30, 250);
			
		}
		@Test
		void Invalid_MEM_Execution_DataOver () {
			// Setup
			String label="not_data";
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new MemAccess( "lw", 1, label );
			assertNotNull( ins );
			// Execution -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Data", addr, "Supported" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Data" ) );
			testLogs_ex._fetching( base_PC );
		}
		@ParameterizedTest ( name="[{index}] LW_Execution with Instr-Label" )
		@ValueSource ( strings={ "instr", "instr_top" } )
		void Invalid_MEM_Execution (String label) {
			// Setup
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new MemAccess( "lw", 1, label );
			assertNotNull( ins );
			// Execution -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Data", addr, "Valid" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Data" ) );
			testLogs_ex._fetching( base_PC );
		}
		@Test
		void Invalid_MEM_Execution_BassOffset_OutOfBounds ( ) {
			// Setup
			int imm=40;
			int addr=DataMemory.BASE_DATA_ADDRESS-4;
			values[ 30 ]=(addr - imm);
			values[ 1 ]=0;
			data.put( 0, 200.0 );
			// - Expect after load, Register #1 will have the value 200
			// Build
			Instruction ins=new MemAccess( "lw",  30, 1, imm );
			// RS_val + Imm should give the correct address.
			assertNotNull( ins );
			// Execution
			Instr.assembleAndExecute_Throws(IndexOutOfBoundsException.class, ins);
			// Result
			assertEquals( 0, values[ 1 ] );	// Not Modified
			// Output
			testLogs_ex.load_output_before_exception( base_PC, 30, values[30], imm, addr );
			// Output cut off by exception
		}
		@Test
		void Invalid_MEM_Execution_BassOffset_NotAligned ( ) {
			// Setup
			int imm=40;
			int addr=DataMemory.BASE_DATA_ADDRESS+3;
			values[ 30 ]=(addr - imm);
			values[ 1 ]=0;
			data.put( 0, 200.0 );
			// - Expect after load, Register #1 will have the value 200
			// Build
			Instruction ins=new MemAccess( "lw", 30, 1, imm  );
			// RS_val + Imm should give the correct address.
			assertNotNull( ins );
			// Execution
			Instr.assembleAndExecute_Throws(IllegalArgumentException.class, ins);
			// Result
			assertEquals( 0, values[ 1 ] );	// Not Modified
			// Output
			testLogs_ex.load_output_before_exception( base_PC, 30, values[30], imm, addr);
			// Output cut off by exception
		}
		
	}
	
	@Nested
	class IMMEDIATE_BRANCH {	// Bug where Imm 2 is set to 40, for some reason
		
		private int takenPC(int imm){
			return 0x00400000+4+(4*imm);
		}
		
		@Nested
		class Taken {
			@Test
			void BEQ_Execution ( ) {
				Instruction ins=new Branch( "beq", 5, 5, 20 );
				assertNotNull( ins );
				// Execution & Result
				Instr.assembleAndExecute_newPC( takenPC(20), ins );
				testLogs_ex.Branch_output(base_PC, "beq",5, 0, 5, 0, 20, "^", true,true);
			}
			@Test
			void BNE_Execution ( ) {
				values[ 2 ]=40;
				values[ 3 ]=41;
				Instruction ins=new Branch( "bne", 2, 3, 30 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_newPC( takenPC(30), ins );
				testLogs_ex.Branch_output(base_PC, "bne",2, 40, 3, 41, 30, "^", false,true);
			}
			@Test
			void BLT_Execution ( ) {
				values[ 2 ]=40;
				values[ 3 ]=41;
				Instruction ins=new Branch( "blt", 2, 3, 40 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_newPC( takenPC(40), ins );
				testLogs_ex.Branch_output(base_PC, "blt",2, 40, 3, 41, 40, "<", false,true);
			}
			@Test
			void BGE_Execution_Equal ( ) {
				Instruction ins=new Branch( "bge", 5, 5, 50 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_newPC( takenPC(50), ins );
				testLogs_ex.Branch_output(base_PC, "bge",5, 0, 5, 0, 50, "<", true,true);
			}
			@Test
			void BGE_Execution ( ) {
				values[ 2 ]=42;
				values[ 3 ]=41;
				Instruction ins=new Branch( "bge", 2, 3, 60 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_newPC( takenPC(60), ins );
				testLogs_ex.Branch_output(base_PC, "bge",2, 42, 3, 41,60, "<", true,true);
			}
			@Test
			void BGT_Execution ( ) {
				values[ 2 ]=41;
				values[ 3 ]=42;
				Instruction ins=new Branch( "bgt", 3, 2, 70 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_newPC( takenPC(70), ins );
				testLogs_ex.Branch_output(base_PC, "bgt",3, 42, 2, 41, 70, "<=", true,true);
			}
			@Test
			void BLE_Execution_Equal ( ) {
				Instruction ins=new Branch( "ble", 5, 5, 80 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_newPC( takenPC(80), ins );
				testLogs_ex.Branch_output(base_PC, "ble",5, 0, 5, 0, 80, "<=", false,true);
			}
			@Test
			void BLE_Execution ( ) {
				values[ 2 ]=41;
				values[ 3 ]=43;
				Instruction ins=new Branch( "ble", 2, 3, 90 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_newPC( takenPC(90), ins );
				testLogs_ex.Branch_output(base_PC, "ble",2, 41, 3, 43, 90, "<=", false,true);
			}
		}
		
		@Nested
		class Not_Taken {
			@Test
			void BEQ_Execution ( ) {
				values[ 2 ]=40;
				values[ 3 ]=41;
				Instruction ins=new Branch( "beq", 2, 3, 20 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_incPC( ins );
				testLogs_ex.Branch_output(base_PC, "beq",2, 40, 3, 41, 20, "^", true,false);
			}
			@Test
			void BNE_Execution ( ) {
				Instruction ins=new Branch( "bne", 5, 5, 30 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_incPC( ins );
				testLogs_ex.Branch_output(base_PC, "bne",5, 0, 5, 0, 30, "^", false,false);
			}
			@Test
			void BLT_Execution ( ) {
				values[ 2 ]=40;
				values[ 3 ]=41;
				Instruction ins=new Branch( "blt", 3, 2, 40 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_incPC( ins );
				testLogs_ex.Branch_output(base_PC, "blt",3, 41, 2, 40, 40, "<", false,false);
			}
			@Test
			void BGE_Execution ( ) {
				values[ 2 ]=42;
				values[ 3 ]=41;
				Instruction ins=new Branch( "bge", 3, 2, 60 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_incPC( ins );
				testLogs_ex.Branch_output(base_PC, "bge", 3, 41,2, 42,60, "<", true,false);
			}
			@Test
			void BGT_Execution ( ) {
				values[ 2 ]=30;
				values[ 3 ]=42;
				Instruction ins=new Branch( "bgt", 2, 3, 70 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_incPC( ins );
				testLogs_ex.Branch_output(base_PC, "bgt", 2, 30,3, 42, 70, "<=", true,false);
			}
			@Test
			void BLE_Execution ( ) {
				values[ 2 ]=41;
				values[ 3 ]=43;
				Instruction ins=new Branch( "ble", 3, 2, 90 );
				// Execution & Result
				assertNotNull( ins );
				Instr.assembleAndExecute_incPC( ins );
				testLogs_ex.Branch_output(base_PC, "ble",3, 43, 2, 41, 90, "<=", false,false);
			}
		}
		
	}
	@Nested
	class JUMP {
		
		private final String label="instr_top";
		
		@Test
		void Jump_Execution_Label ( ) {
			// Setup
			int addr=labelMap.get( label );
			// Build
			Instruction ins=new J_Type( "j", label );
			assertNotNull( ins );
			// Execution & Result
			Instr.assembleAndExecute_newPC( addr, ins );
			// Output
			testLogs_ex.J_output( base_PC, addr/4);
		}
		
		@Test
		void Jump_Execution_IMM ( ) {
			// Setup
			int addr=0x00400014; // index 21
			Instruction ins=new J_Type( "j", addr/4 );
			assertNotNull( ins );
			// Execution & Result
			Instr.assembleAndExecute_newPC( addr, ins );
			// Output
			testLogs_ex.J_output( base_PC, addr/4);
		}
		@Test
		void JumpAndLink_Execution_Label  ( ) {
			// Setup
			int addr=labelMap.get( label );
			// Build
			Instruction ins=new J_Type( "jal",  label );
			assertNotNull( ins );
			// Execution & Result
			Instr.assembleAndExecute_newPC( addr, ins );
			// Output
			testLogs_ex.jal_output( base_PC, addr/4);
		}
		
		@Test
		void Invalid_Jump_Execution_InstrOver ( ) {
			// Setup
			String label="not_instr";
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new J_Type( "j",  label );
			assertNotNull( ins );
			// Execution -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Instruction", addr, "Supported" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Instruction" ) );
			testLogs_ex._fetching( base_PC );
		}
		@ParameterizedTest ( name="[{index}] Jump_Execution with Data-Label" )
		@ValueSource ( strings={ "data", "data_top" } )
		void Invalid_Jump_Execution (String label) {
			// Setup
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new J_Type( "j", label );
			assertNotNull( ins );
			// Execution -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Instruction", addr, "Valid" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Instruction" ) );
			testLogs_ex._fetching( base_PC );
		}
		
		@Test
		void InvalidJUMP_Execution_IMM_OutOfBounds ( ) {    // Not possible to have misaligned address due ot imm->addr conversion
			// Setup
			int addr=5242884;
			// Build
			Instruction ins=new J_Type( "jal", addr/4 );
			assertNotNull( ins );
			// Execution & Result
			Instr.assembleAndExecute_newPC( addr, ins );	// NPC = Addr
			// Output
			testLogs_ex.jal_output( base_PC, addr/4 );
			// Exception is thrown when trying to retrieve the address from the InstrMemory
			InstrMemory instrMemory = new InstrMemory( new ArrayList<>(), testLogs.actualExecution);
			assertThrows(IndexOutOfBoundsException.class, ()->instrMemory.InstructionFetch(addr) );
		}
	}
	
	private static class Instr {
		/**
		 For a given instruction, Tests: It assembles -> Then Executes without errors.
		 <p> and the returned PC, matches the PC+4
		 */
		private static void assembleAndExecute_incPC (Instruction ins) {
			assembleAndExecute_newPC( 0x00400004, ins );    //NPC = PC+4
		}
		/**
		 For a given instruction, Tests: It assembles -> Then Executes without errors.
		 <p> and the returned PC, matches the given newPC
		 */
		private static void assembleAndExecute_newPC (Integer newPC, Instruction ins) {
			assertTrue( ins.assemble( errors, labelMap, 0x00400000) );
			assertEquals( newPC, testLogs_ex.pipeline( ins) );
		}
		/**
		 For a given instruction, Tests: It Fails assembly ->
		 Then attempting to execute makes it throw an exception
		 */
		private static void failAssemble_andExecuteThrows (Instruction ins) {
			assertFalse( ins.assemble( errors, labelMap, 0x00400000) );
			assertThrows( IllegalStateException.class, ( ) -> testLogs_ex.pipeline( ins ) );
		}
		/**
		 For a given instruction, Tests: It assembles ->
		 Then attempting to execute makes it throw an exception
		 <p>For Address based instructions -> pointed to the wrong address
		 */
		private static <T extends Throwable> void assembleAndExecute_Throws (Class<T> exception, Instruction ins) {
			assertTrue( ins.assemble( errors, labelMap, 0x00400000) );
			assertThrows( exception, ( ) -> testLogs_ex.pipeline( ins ) );
		}
	}
	
}
