package model.components;

import _test.Tags;
import _test.TestLogs;
import _test.providers.AddressProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import model.I_Type;
import model.Instruction;
import model.Nop;
import model.R_Type;

import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
@Tag( Tags.Pkg.MOD )
@Tag( Tags.Pkg.COM )
@Tag( Tags.INSTR )
@Tag( Tags.MEM )
@Tag( Tags.ACC )
@DisplayName ( Tags.Pkg.MOD + " : " + Tags.Pkg.COM + " : "+Tags.INSTR+Tags.MEM+" Test" )
class InstrMemoryTest {
	// Ignored
	private static final ExecutionLog lg = new ExecutionLog( new ArrayList<>() );
	private static final DataMemory dm = new DataMemory( new HashMap<>(), lg);
	private static final RegisterBank rm = new RegisterBank( new int[32], lg );
	// Ok - Actual stuff
	private static TestLogs testLogs;
	private static final ArrayList<Instruction> instr_list= new ArrayList<>();
	private static InstrMemory instrMemory;
	
	@BeforeEach
	void setUp ( ) {
		testLogs = new TestLogs();
		instrMemory = new InstrMemory( instr_list, testLogs.actualExecution);
	}
	
	@AfterAll
	static void afterAll ( ) {
		testLogs.after();
	}
	@Test
	@Tag( Tags.OUT )
	void InstructionFetch() {
		Integer PC = 0x00400000;
		Instruction ins;
		
		instr_list.add( new R_Type( "add", 1, 1, 1 ));//0 -> 4
		instr_list.add( new R_Type( "sub", 1,1,1 ));//4 -> 8
		instr_list.add( new I_Type( "addi", 1, 1, -40));//8 -> 12
		instr_list.add( new Nop( "exit" ) );
		
		for ( Instruction i: instr_list ){
			i.assemble(testLogs.actualErrors, new HashMap<>( ) );
			
			ins = instrMemory.InstructionFetch(PC);
			testLogs.expectedExecution.append( TestLogs.FMT_MSG._Execution._fetch(PC) );
			assertEquals( i, ins );
			PC = ins.execute( PC, dm, rm, lg );
		}
		assertNull(PC);
	}
	
	@Test
	@Tag( Tags.OUT )
	void InstructionFetch_Supported_OverBounds() {
		// Actual
		Instruction ins = instrMemory.InstructionFetch(0x00500000 );
		// Pre-Assembled
		ins.execute(0x00500000, dm, rm, testLogs.actualExecution );
		
		// Expected Pre Assembled Exit Instruction
		Instruction expected = new Nop("exit");
		expected.assemble( testLogs.expectedErrors, new HashMap<>() );
		testLogs.expectedExecution.append( TestLogs.FMT_MSG._Execution._fetch(0x00500000 ) );
		testLogs.expectedExecution.appendEx( "\tRun Over Provided Instructions" );
		expected.execute(0x00500000, dm, rm, testLogs.expectedExecution );
	}
	
	@ParameterizedTest (name = "[{index}] == InstructionFetch - Invalid Address[{arguments}] :: Not Supported")
	@ArgumentsSource( AddressProvider.InstrAddr.Supported.Not_Supported_Boundaries.class )
	void Invalid_InstructionFetch_NotSupported_OverBounds( String hexAddr, Integer address ) {
		assertThrows( IndexOutOfBoundsException.class, ()-> instrMemory.InstructionFetch( address ) );
	}
	
	@ParameterizedTest (name = "[{index}] == InstructionFetch - Invalid Address[{arguments}] :: NotAligned")
	@ArgumentsSource( AddressProvider.InstrAddr.Supported.Not_Aligned.class )
	void Invalid_InstructionFetch_NotAligned ( String hexAddr, Integer address ) {
		assertThrows( IllegalArgumentException.class, ()-> instrMemory.InstructionFetch( address ) );
	}
}
