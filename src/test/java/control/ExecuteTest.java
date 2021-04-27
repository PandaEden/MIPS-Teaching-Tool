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
	private static Execution execution;
	private static final HashMap<Integer, Double> data=new HashMap<>( );
	private static final ArrayList<Instruction> instr_list= new ArrayList<>();
	private static final int[] values=new int[ 32 ];
	private static ExecutionLog actual;
	
	@BeforeAll
	static void beforeAll ( ) {
		testLogs= new TestLogs();
		actual = testLogs.actualExecution;
	}
	@BeforeEach
	void setUp ( ) {
		execution= new Execution( actual, testLogs.actualErrors,  new DataMemory( data, actual ), new RegisterBank( values, actual ), instr_list);
	}
	@AfterEach
	void tearDown ( ) {
		actual.println();
		actual.clear();	// Skip checking Output, other than for errors
		
		testLogs.after();
		execution.reset();
		instr_list.clear();
		data.clear();
		for ( int i=1; i<32; i++ ) values[i]=0; /*reset Register Bank values*/
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
		execution.runToEnd();
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
	@Tag( Tags.EX )
	void Test_Result_Run_Interrupted_ByError ( ) {
		values[30]=0x10010005;
		instr_list.add( new MemAccess( "lw", 30, 1, 40 ) );//0 -> * ERROR
		instr_list.add( new R_Type( "add", 1,1,1 )); // Not Run
		instr_list.forEach( i -> i.assemble( testLogs.actualErrors, InstrProvider.labelsMap, 0x00400000) ); // ASSEMBLE
		//Execution -> Run To
		execution.runToEnd();
		// Result
		assertEquals( 0x10010005, values[30] );
		for ( int i =0; i<30;i++ ){
			assertEquals(0, values[i]);
		}
		assertEquals( 0,values[31] );
		assertTrue( data.isEmpty() );
		testLogs.expectedErrors.appendEx( "Data Address [0x1001002D, 268501037] Must Be DoubleWord Aligned" );
	}
	//TODO - add   [Jump to invalid Instr Addr]
}
