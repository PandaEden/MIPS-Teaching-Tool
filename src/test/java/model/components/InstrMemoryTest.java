package model.components;

import _test.Tags;
import _test.TestLogs;
import _test.providers.AddressProvider;
import _test.providers.InstrProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import model.Instruction;
import model.instr.Operands;

import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
@Tag( Tags.Pkg.MOD )
@Tag( Tags.Pkg.COM )
@Tag( Tags.INSTR )
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
		
		instr_list.add( Instruction.buildInstruction( "add", InstrProvider.RD_RS_RT.operands ));
		instr_list.add( Instruction.buildInstruction( "sub", InstrProvider.RD_RS_RT.operands ));
		instr_list.add( Instruction.buildInstruction( "addi", InstrProvider.I.RT_RS_IMM.operands ));
		instr_list.add( Instruction.buildInstruction( "exit", Operands.getExit() ) );
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
		
		for ( Instruction i: instr_list ){
			i.assemble(testLogs.actualErrors, new HashMap<>() );
			
			ins = instrMemory.InstructionFetch(PC);
			testLogs.expectedExecution.append(TestLogs.FMT_MSG._Execution._fetch(PC));
			assertEquals(i, ins);
			PC = ins.execute(PC, dm, rm, lg);
		}
		assertNull(PC);
	}
	
	@Test
	@Tag( Tags.OUT )
	void InstructionFetch_Supported_OverBounds() {
		// Actual
		Instruction ins = instrMemory.InstructionFetch(0x00500000);
		ins.assemble(testLogs.actualErrors, new HashMap<>());
		ins.execute(0x00500000, dm, rm, testLogs.actualExecution );
		
		// Expected
		Instruction expected = Instruction.buildInstruction("exit", Operands.getExit());
		expected.assemble(testLogs.expectedErrors, new HashMap<>());
		testLogs.expectedExecution.append(TestLogs.FMT_MSG._Execution._fetch(0x00500000));
		testLogs.expectedExecution.appendEx( "\tRun Over Provided Instructions" );
		expected.execute(0x00500000, dm, rm, testLogs.expectedExecution);
	}
	
	@ParameterizedTest
	@ArgumentsSource( AddressProvider.InstrAddr.Supported.Not_Supported_Boundaries.class )
	void Invalid_InstructionFetch_NotSupported_OverBounds(String hexAddr, Integer address) {
		assertThrows(IndexOutOfBoundsException.class, ()->instrMemory.InstructionFetch(address));
	}
	
	@ParameterizedTest
	@ArgumentsSource( AddressProvider.InstrAddr.Supported.Not_Aligned.class )
	void Invalid_InstructionFetch_NotAligned (String hexAddr, Integer address) {
		assertThrows(IllegalArgumentException.class, ()->instrMemory.InstructionFetch(address));
	}
}
