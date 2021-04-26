package control;

import _test.Tags;
import _test.TestLogs;
import _test.providers.InstrProvider;
import org.junit.jupiter.api.*;

import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.*;

import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag( Tags.EX )
class ExecuteTest {
	private static TestLogs testLogs;
	private static TestLogs.FMT_MSG._Execution testLogs_ex;
	private static Execution execution;
	private static final HashMap<Integer, Double> data=new HashMap<>( );
	private static final ArrayList<Instruction> instr_list= new ArrayList<>();
	private static final int[] values=new int[ 32 ];
	private static ExecutionLog actual;
	private static ExecutionLog expected;
	
	@BeforeAll
	static void beforeAll ( ) {
		testLogs= new TestLogs();
		actual = testLogs.actualExecution;
		expected = testLogs.expectedExecution;
		testLogs_ex = new TestLogs.FMT_MSG._Execution( values, data, actual, expected );
	}
	@BeforeEach
	void setUp ( ) {
		execution= new Execution( actual, new DataMemory( data, actual ), new RegisterBank( values, actual ), instr_list);
	}
	@AfterEach
	void tearDown ( ) {
		testLogs.after();
		execution.reset();
		instr_list.clear();
		data.clear();
		for ( int i=1; i<32; i++ ) values[i]=0; /*reset Register Bank values*/
	}
	
	/**Takes the Output from the expectedExecutionLog
	 and current State of the Register Bank.
	 <p>And appends it to the given StringBuilder.
	 <p>Then clears the expected Log. */
	private void appendExpectedOutput_AndClear (StringBuilder sb){
		sb.append( expected);
		expected.clear();
	}
	
	@Test
	@Tag( Tags.OUT )
	void Test_Output_RunToEnd ( ) {
		values[1]=4;
		instr_list.add( new R_Type( "add", 1,1,2 ));//0 -> 4
		instr_list.add( new J_Type( "j",0x00100003 ));//4 -> J: 12
		instr_list.add( new R_Type( "sub", 1,1,1 ));//8 - skipped
		instr_list.add( new I_Type( "addi", 1, 1, -40));//12 <-
		instr_list.forEach( i -> i.assemble( testLogs.actualErrors, InstrProvider.labelsMap, 0x00400000) ); // ASSEMBLE
		// 16 (0x10) ,  <- AutoExit
		
		// Output
		StringBuilder expectedOutput=new StringBuilder();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 0\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 4\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Add [0x00400000] Add $2, $1, $1
		testLogs_ex.R_output(0x00400000, "add",1,4 , 1, 4, 2, 8,"+");
		appendExpectedOutput_AndClear(expectedOutput);
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 0\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 4\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|*R2: 8\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Jump [0x00400004] Jump -> 0x0040000C
		testLogs_ex.J_output(0x00400004, 0x00100003);
		appendExpectedOutput_AndClear(expectedOutput);
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 0\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 4\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|*R2: 8\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Addi [0x0040000C] Addi $1, $1, -40
		testLogs_ex.I_output( 0x0040000C, "addi", 1, 4, 1, -36, -40,"+");
		appendExpectedOutput_AndClear(expectedOutput);
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 0\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|*R1: -36\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 8\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Exit [0x00400010] autoExit
		testLogs_ex.auto_exit_output( 0x00400010);
		appendExpectedOutput_AndClear(expectedOutput);
		// Collect Expected Output
		
		// Execution -> Run To
		StringBuilder actualOutput = new StringBuilder();
		execution.RunToEnd( instr_list, actualOutput);
		actualOutput.append( testLogs.actualErrors);
		actualOutput.append( testLogs.actualWarnings);
		assertEquals(expectedOutput.toString(), actualOutput.toString());
		
		//Print Output
		TestLogs.tempPrint(actualOutput.toString());
	}
	
	@Test
	@Tag( Tags.EX )
	void Test_Result_RunToEnd ( ) {
		values[1]=4;
		instr_list.add( new R_Type( "add", 1,1,2 ));//0 -> 4
		instr_list.add( new J_Type( "j",0x00100003 ));//4 -> J: 12
		instr_list.add( new R_Type( "sub", 1,1,1 ));//8 - skipped
		instr_list.add( new I_Type( "addi", 1, 1, -40));//12 <-
		instr_list.forEach( i -> i.assemble( testLogs.actualErrors, InstrProvider.labelsMap, 0x00400000) ); // ASSEMBLE
		// 16 (0x10) ,  <- AutoExit
		
		// Execution -> Run To
		StringBuilder actualOutput = new StringBuilder();
		execution.RunToEnd( instr_list, actualOutput);
		// Check Values
		// values[0] is immutable
		assertEquals(-36, values[1]);
		assertEquals(8, values[2]);
		for ( int i =3; i<values.length;i++ ){
			assertEquals(0, values[i]);
		}
		assertTrue( data.isEmpty() );
	}
	
	// TODO nest these tests and move setup portion, and Execution to BeforeEach
	
	@Test
	@Tag( Tags.OUT )
	void Test_Output_Run_Interrupted_ByError ( ) {
		values[30]=0x10010005;
		instr_list.add( new MemAccess( "lw", 30, 1, 40 ) );//0 -> * ERROR
		instr_list.add( new R_Type( "add", 1,1,1 )); // Not Run
		instr_list.forEach( i -> i.assemble( testLogs.actualErrors, InstrProvider.labelsMap, 0x00400000) ); // ASSEMBLE
		
		// Output
		StringBuilder expectedOutput=new StringBuilder();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 0\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 268500997|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		testLogs_ex.load_output_before_exception(0x00400000, 30, values[30], 40,0x1001002D );
		appendExpectedOutput_AndClear(expectedOutput); // Due to the interrupt, the ExLog is not cleared as it doesn't finish execution
		expectedOutput.append( "ERROR: Data Address [0x1001002D, 268501037] Must Be DoubleWord Aligned!" );
		
		//Execution -> Run To
		StringBuilder actualOutput = new StringBuilder();
		execution.RunToEnd( instr_list, actualOutput);
		assertEquals(expectedOutput.toString(), actualOutput.toString());
	}
	@Test
	@Tag( Tags.EX )
	void Test_Result_Run_Interrupted_ByError ( ) {
		values[30]=0x10010005;
		instr_list.add( new MemAccess( "lw", 30, 1, 40 ) );//0 -> * ERROR
		instr_list.add( new R_Type( "add", 1,1,1 )); // Not Run
		instr_list.forEach( i -> i.assemble( testLogs.actualErrors, InstrProvider.labelsMap, 0x00400000) ); // ASSEMBLE
		//Execution -> Run To
		StringBuilder actualOutput = new StringBuilder();
		execution.RunToEnd( instr_list, actualOutput);
		// Result
		assertEquals( 0x10010005, values[30] );
		for ( int i =0; i<30;i++ ){
			assertEquals(0, values[i]);
		}
		assertEquals( 0,values[31] );
		assertTrue( data.isEmpty() );
		
	}
	//TODO - add   [Jump to invalid Instr Addr]
}
