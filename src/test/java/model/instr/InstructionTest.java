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
	private static final Integer PC=0x004000C8; // set PC to instr index #51
	private static final String _PC=Convert.int2Hex( PC );
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
		// Execute
		Instr.assembleAndExecute_newPC(null, ins );
		// Output
		testLogs_ex.exit_output( _PC, opcode );
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
			//Execute
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( 60, values[ 5 ] );
			// Output
			testLogs_ex.R_output(_PC, "add", 6, 25, 7, 35, 5, 60);
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
			// Execute
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( -4750, values[ 16 ] );
			// Output
			testLogs_ex.R_output(_PC, "sub", 20, 250, 24, 5000, 16, -4750);
			
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
			// Execute
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( -150, values[ 1 ] );
			// Output
			testLogs_ex.I_output( _PC, "addi", 30, 250, 1, -150, -400 );
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
			// Execute
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( 20, values[ 1 ] );
			// Output
			testLogs_ex.load_output( _PC, 0, 0, addr, 1, 20);
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
			// Execute
			Instr.assembleAndExecute_incPC( ins );
			// Result
			assertEquals( 200, values[ 1 ] );
			// Output
			testLogs_ex.load_output( _PC, 30, values[ 30 ], imm, 1, 200);
			
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
			// Execute
			Instr.assembleAndExecute_incPC( ins );
			// Results
			assertEquals( 250, data.get( 0 ) ); // value at address has changed to expected value
			// Output
			testLogs_ex.store_output( _PC, 0, 0, addr, 30, 250);
			
		}
		@Test
		void Invalid_MEM_Execution_DataOver () {
			// Setup
			String label="not_data";
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new MemAccess( "lw", 1, label );
			assertNotNull( ins );
			// Execute -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Data", addr, "Supported" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Data" ) );
		}
		@ParameterizedTest ( name="[{index}] LW_Execution with Instr-Label" )
		@ValueSource ( strings={ "instr", "instr_top" } )
		void Invalid_MEM_Execution (String label) {
			// Setup
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new MemAccess( "lw", 1, label );
			assertNotNull( ins );
			// Execute -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Data", addr, "Valid" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Data" ) );
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
			// Execute
			Instr.assembleAndExecute_Throws(IndexOutOfBoundsException.class, ins);
			// Result
			assertEquals( 0, values[ 1 ] );	// Not Modified
			// Output
			testLogs_ex.decode( _PC, "lw", "IMMEDIATE" );
			testLogs_ex.rb_read( values[ 30 ], 30 );
			testLogs_ex.imm_cal_addr( imm, values[ 30 ], addr );
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
			// Execute
			Instr.assembleAndExecute_Throws(IllegalArgumentException.class, ins);
			// Result
			assertEquals( 0, values[ 1 ] );	// Not Modified
			// Output
			testLogs_ex.decode( _PC, "lw", "IMMEDIATE" );
			testLogs_ex.rb_read( values[ 30 ], 30 );
			testLogs_ex.imm_cal_addr( imm, values[ 30 ], addr );
			// Output cut off by exception
		}
		
	}
	
	@Nested
	class JUMP {
		
		@Test
		void Jump_Execution_Label ( ) {
			// Setup
			int addr=labelMap.get( "instr" );
			// Build
			Instruction ins=new J_Type( "j", "instr" );
			assertNotNull( ins );
			// Execute & Result
			Instr.assembleAndExecute_newPC( addr, ins );
			// Output
			testLogs_ex.J_output( _PC, addr/4);
		}
		
		@Test
		void Jump_Execution_IMM ( ) {
			// Setup
			int addr=0x00400014; // index 21
			Instruction ins=new J_Type( "j", addr/4 );
			assertNotNull( ins );
			// Execute & Result
			Instr.assembleAndExecute_newPC( addr, ins );
			// Output
			testLogs_ex.J_output( _PC, addr/4);
		}
		@Test
		@DisplayName ( "Instruction JumpAndLink" )
		void instructionTestJumpAndLink ( ) {
			// Setup
			String label="instr";
			int addr=labelMap.get( label );
			// Build
			Instruction ins=new J_Type( "jal",  label );
			assertNotNull( ins );
			// Execute & Result
			Instr.assembleAndExecute_newPC( addr, ins );
			// Output
			testLogs_ex.jal_output( _PC, addr/4);
		}
		
		@Test
		void Invalid_Jump_Execution_InstrOver ( ) {
			// Setup
			String label="not_instr";
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new J_Type( "j",  label );
			assertNotNull( ins );
			// Execute -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Instruction", addr, "Supported" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Instruction" ) );
		}
		@ParameterizedTest ( name="[{index}] Jump_Execution with Data-Label" )
		@ValueSource ( strings={ "data", "data_top" } )
		void Invalid_Jump_Execution (String label) {
			// Setup
			String addr=Convert.int2Hex( labelMap.get( label ) );
			// Build
			Instruction ins=new J_Type( "j", label );
			assertNotNull( ins );
			// Execute -> Errors
			Instr.failAssemble_andExecuteThrows( ins );
			// Output
			testLogs.expectedErrors.appendEx( FMT_MSG.xAddressNot( "Instruction", addr, "Valid" ) );
			testLogs.expectedErrors.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Instruction" ) );
		}
		
		@Test
		void InvalidJUMP_Execution_IMM_OutOfBounds ( ) {    // Not possible to have misaligned address due ot imm->addr conversion
			// Setup
			int addr=5242884;
			// Build
			Instruction ins=new J_Type( "jal", addr/4 );
			assertNotNull( ins );
			// Execute & Result
			Instr.assembleAndExecute_newPC( addr, ins );	// NPC = Addr
			// Output
			testLogs_ex.jal_output( _PC, addr/4 );
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
			assembleAndExecute_newPC( PC + 4, ins );    //NPC = PC+4
		}
		/**
		 For a given instruction, Tests: It assembles -> Then Executes without errors.
		 <p> and the returned PC, matches the given newPC
		 */
		private static void assembleAndExecute_newPC (Integer newPC, Instruction ins) {
			assertTrue( ins.assemble( errors, labelMap ) );
			assertEquals( newPC, testLogs_ex.execute( PC, ins) );
		}
		/**
		 For a given instruction, Tests: It Fails assembly ->
		 Then attempting to execute makes it throw an exception
		 */
		private static void failAssemble_andExecuteThrows (Instruction ins) {
			assertFalse( ins.assemble( errors, labelMap ) );
			assertThrows( IllegalStateException.class, ( ) -> testLogs_ex.execute( PC, ins ) );
		}
		/**
		 For a given instruction, Tests: It assembles ->
		 Then attempting to execute makes it throw an exception
		 <p>For Address based instructions -> pointed to the wrong address
		 */
		private static <T extends Throwable> void assembleAndExecute_Throws (Class<T> exception, Instruction ins) {
			assertTrue( ins.assemble( errors, labelMap ) );
			assertThrows( exception, ( ) -> testLogs_ex.execute( PC, ins) );
		}
	}
	
}
