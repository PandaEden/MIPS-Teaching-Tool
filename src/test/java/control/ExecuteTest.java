package control;

import _test.Tags;
import _test.TestLogs;
import _test.providers.InstrProvider;
import org.junit.jupiter.api.*;

import model.Instruction;
import model.instr.Operands;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@Tag( Tags.EX )
class ExecuteTest {
	private static TestLogs logs;
	private static TestLogs.FMT_MSG._Execution testLogs_ex;
	private static Execute execute;
	private static final HashMap<Integer, Double> data=new HashMap<>( );
	ArrayList<Instruction> instr_list= new ArrayList<>();
	private static int[] values=new int[ 32 ];
	
	
	@BeforeAll
	static void beforeAll ( ) {
		logs = new TestLogs();
		testLogs_ex = new TestLogs.FMT_MSG._Execution( values, data, logs.actualExecution, logs.expectedExecution );
		
		execute = new Execute(logs.actualExecution, data, values);
	}
	@AfterEach
	void tearDown ( ) {
		logs.after();
		instr_list.clear();
		for ( int i=1; i<32; i++ ) values[i]=0; /*reset values*/
	}
	
	/**Takes the Output from the expectedExecutionLog
	 and current State of the Register Bank.
	 <p>And appends it to the given StringBuilder.
	 <p>Then clears the expected Log. */
	void appendExpectedOutput_AndClear (StringBuilder sb){
		sb.append(logs.expectedExecution);
		logs.expectedExecution.clear();
	}
	
	@Test
	void RunToEnd ( ) {
		values[1]=4;
		instr_list.add( Instruction.buildInstruction( "add", InstrProvider.RD_RS_RT.operands ));//0 -> 4
		instr_list.add( Instruction.buildInstruction( "j", new Operands(0x00100003) ));//4 -> 12
		instr_list.add( Instruction.buildInstruction( "sub", InstrProvider.RD_RS_RT.operands ));//8 - skipped
		instr_list.add( Instruction.buildInstruction( "addi", InstrProvider.I.RT_RS_IMM.operands ));//12 <-
		instr_list.forEach( i -> i.assemble(logs.actualErrors, InstrProvider.labelsMap) ); // ASSEMBLE
		// 16 (0x10) ,  <- AutoExit
		
		// Output
		StringBuilder expectedOutput=new StringBuilder();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\t\tR4: 0\tR8: 0\tR12: 0\t\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 4\t\tR5: 0\tR9: 0\tR13: 0\t\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\t\tR6: 0\tR10: 0\tR14: 0\t\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\t\tR7: 0\tR11: 0\tR15: 0\t\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n"
		);
		// Add [0x00400000] Add $2, $1, $1
		logs.expectedExecution.append(TestLogs.FMT_MSG._Execution._fetch(0x00400000));
		testLogs_ex.R_output("0x00400000", "add",1,4 , 1, 4, 2, 8);
		appendExpectedOutput_AndClear(expectedOutput);
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\t\tR4: 0\tR8: 0\tR12: 0\t\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 4\t\tR5: 0\tR9: 0\tR13: 0\t\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|*R2: 8\t\tR6: 0\tR10: 0\tR14: 0\t\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\t\tR7: 0\tR11: 0\tR15: 0\t\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n"
		);
		// Jump [0x00400004] Jump -> 0x0040000C
		logs.expectedExecution.append(TestLogs.FMT_MSG._Execution._fetch(0x00400004));
		testLogs_ex.J_output("0x00400004", 0x00100003);
		appendExpectedOutput_AndClear(expectedOutput);
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\t\tR4: 0\tR8: 0\tR12: 0\t\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 4\t\tR5: 0\tR9: 0\tR13: 0\t\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 8\t\tR6: 0\tR10: 0\tR14: 0\t\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\t\tR7: 0\tR11: 0\tR15: 0\t\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n"
		);
		// Addi [0x0040000C] Addi $1, $1, -40
		logs.expectedExecution.append(TestLogs.FMT_MSG._Execution._fetch(0x0040000C));
		testLogs_ex.I_output( "0x0040000C", "addi", 1, 4, -40, 1, -36 );
		appendExpectedOutput_AndClear(expectedOutput);
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\t\tR4: 0\tR8: 0\tR12: 0\t\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|*R1: -36\t\tR5: 0\tR9: 0\tR13: 0\t\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 8\t\tR6: 0\tR10: 0\tR14: 0\t\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\t\tR7: 0\tR11: 0\tR15: 0\t\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n"
		);
		// Exit [0x00400010] autoExit
		logs.expectedExecution.append(TestLogs.FMT_MSG._Execution._fetch(0x00400010));
		testLogs_ex.run_over();
		testLogs_ex.exit_output( "0x00400010", "exit");
		appendExpectedOutput_AndClear(expectedOutput);
		// Collect Expected Output
		
		// Execute -> Run To
		StringBuilder actualOutput = new StringBuilder();
		execute.execute(instr_list, actualOutput);
		actualOutput.append(logs.actualErrors);
		actualOutput.append(logs.actualWarnings);
		assertEquals(expectedOutput.toString(), actualOutput.toString());
		
		//Print Output
		TestLogs.tempPrint(actualOutput.toString());
	}
	
	@Test
	void Run_Interrupted_ByError ( ) {
		values[30]=0x10010005;
		instr_list.add( Instruction.buildInstruction( "lw", new Operands( "lw", 30, 1, 40 ) ));//0 -> * ERROR
		instr_list.add( Instruction.buildInstruction( "add", InstrProvider.RD_RS_RT.operands )); // Not Run
		instr_list.forEach( i -> i.assemble(logs.actualErrors, InstrProvider.labelsMap) ); // ASSEMBLE
		
		// Output
		StringBuilder expectedOutput=new StringBuilder();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\t\tR4: 0\tR8: 0\tR12: 0\t\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 0\t\tR5: 0\tR9: 0\tR13: 0\t\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\t\tR6: 0\tR10: 0\tR14: 0\t\t\tR18: 0\tR22: 0\tR26: 0\tR30: 268500997|\n" +
				"|R3: 0\t\tR7: 0\tR11: 0\tR15: 0\t\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n"
		);
		logs.expectedExecution.append(TestLogs.FMT_MSG._Execution._fetch(0x00400000));
		testLogs_ex.decode( "0x00400000", "lw", "IMMEDIATE" );
		testLogs_ex.rb_read( values[ 30 ], 30 );
		testLogs_ex.imm_cal_addr( 40, values[ 30 ], 0x1001002D );
		expectedOutput.append(logs.expectedExecution); // Due to the interrupt, the ExLog is not cleared as it doesn't finish execution
		expectedOutput.append( "ERROR: Data Address [0x1001002D, 268501037] Must Be DoubleWord Aligned!" );
		
		//Execute -> Run To
		StringBuilder actualOutput = new StringBuilder();
		execute.execute(instr_list, actualOutput);
		assertEquals(expectedOutput.toString(), actualOutput.toString());
	}
}
