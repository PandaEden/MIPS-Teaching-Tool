import org.junit.jupiter.api.*;
import _test.TestLogs.FMT_MSG;

import util.ansi_codes.Color;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
	private static final String TEST_RESOURCES_DIR="src" + File.separator + "test" + File.separator + "resources" + File.separator;
	private static final String PARSE_COMPLETE = "Parsing Complete!\n";
	private static final String ASSEMBLE_COMPLETE = "Assembly Complete!\n";
	private static final String EX_COMPLETE = "Execution Complete!\n";
	private static final String END_WITH_ERRORS = "Execution Ended With Errors!\n";
	
	// Setup - redirecting Standard Output
	private static final PrintStream standardOut=System.out;
	private static final ByteArrayOutputStream outputStreamCaptor=new ByteArrayOutputStream( );
	
	@BeforeEach
	void setUp ( ) {
		Color.colorSupport=false;
		System.setOut( new PrintStream( outputStreamCaptor ) );
	}
	@AfterEach
	void tearDown ( ) {
		// Restore original System.Out
		System.setOut( standardOut );
	}
	
	void compareWithSystemOut(StringBuilder sb){
		String actual = outputStreamCaptor.toString( ).trim().replace("\r","")+"\n";
		assertEquals( sb.toString(), actual );
		outputStreamCaptor.reset();
	}
	
	@Test
	@DisplayName ( "Test : Parse -> Assemble --> Execute :: Success" )
	void Successful_Parse_Assemble_Execute ( ) {
		//Setup
		HashMap<Integer, Double> data = new HashMap<>();
		ExecutionLog log = new ExecutionLog( new ArrayList<>() );
		FMT_MSG._Execution _ex = new FMT_MSG._Execution( new int[32], data, log, log);
		//Parse->Assemble->Execute
		Main.main( new String[] { TEST_RESOURCES_DIR + "Execution_NoBranches.s" } );
		
		// Output
		StringBuilder expectedOutput=new StringBuilder();
		expectedOutput.append( PARSE_COMPLETE );
		expectedOutput.append( ASSEMBLE_COMPLETE );
		
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 0\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line5  [0x00400000] LW R8, Y			==> Addr[0x10010008],  value [268500992]
		log.append(FMT_MSG._Execution._fetch(0x00400000));
		_ex.load_output("0x00400000" ,0,0,268501000, 8, 268500992);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\t*R8: 268500992\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line7  [0x00400004] LW R16, 0(R8)		==> Addr[0x10010000], value [50]
		log.append(FMT_MSG._Execution._fetch(0x00400004));
		_ex.load_output_modified("0x00400004" ,8,268500992,0, 16, 50);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\t*R16: 50\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line9  [0x00400008] LW R20, 0x290(R8)	==> Addr[0x10010290], value [-900]
		log.append(FMT_MSG._Execution._fetch(0x00400008));
		_ex.load_output("0x00400008" ,8,268500992,0x290, 20, -900);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\t*R20: -900\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line18 [0x0040000C] ADDI R10, R8, 16	 => 268500992+16 = [268501008]
		log.append(FMT_MSG._Execution._fetch(0x0040000C));
		_ex.I_output( "0x0040000C" , "addi", 8, 268500992, 10, 268501008, 16 );
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\tR20: -900\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\t*R10: 268501008\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line19 [0x00400010] SW R20, 0(R10)	==> Addr[0x10010010], value [-900]
		log.append(FMT_MSG._Execution._fetch(0x00400010));
		_ex.store_output_modified("0x00400010" ,10,0x10010010,0, 20, -900);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\tR20: -900\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 268501008\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line20 [0x00400014] ADD R22, R20, R0	 => val+0, [-900] //Move
		log.append(FMT_MSG._Execution._fetch(0x00400014));
		_ex.R_output("0x00400014" ,"add",20, -900, 0, 0, 22, -900);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\tR20: -900\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 268501008\tR14: 0\t\tR18: 0\t*R22: -900\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line21 [0x00400018] SUB R24, R20, R16	 => (-900 - 50) == [-950]
		log.append(FMT_MSG._Execution._fetch(0x00400018));
		_ex.R_output("0x00400018" ,"sub",20, -900, 16, 50, 24, -950);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\tR20: -900\t*R24: -950\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 268501008\tR14: 0\t\tR18: 0\tR22: -900\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line24 [0x0040001C] ADD R18, R16, R16	 => (50+50) == [100]
		log.append(FMT_MSG._Execution._fetch(0x0040001C));
		_ex.R_output("0x0040001C" ,"add",16, 50, 16, 50, 18, 100);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\tR20: -900\tR24: -950\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 268501008\tR14: 0\t\t*R18: 100\tR22: -900\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line26 [0x00400020] J del				--> Addr[0x0040002C]
		log.append(FMT_MSG._Execution._fetch(0x00400020));
		_ex.J_output( "0x00400020" , 0x0010000B);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\tR20: -900\tR24: -950\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 268501008\tR14: 0\t\tR18: 100\tR22: -900\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// _skipped_ unless branch delay:
		// Line28 [0x0040024] SUB R25, R22, R16 (-900 -100) = [-1000]
		// line29 [0x0040028] _skipped_ Always !
		
		// Line31 [0x0040002C] ADD R20, R18, R22 (100 + -900) = [-800]
		log.append(FMT_MSG._Execution._fetch(0x0040002C));
		_ex.R_output("0x0040002C" ,"add",18, 100, 22, -900, 20, -800);
		expectedOutput.append(log); log.clear();
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 268500992\tR12: 0\t\tR16: 50\t*R20: -800\tR24: -950\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 268501008\tR14: 0\t\tR18: 100\tR22: -900\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line-1 [0x00400030] autoExit
		log.append(FMT_MSG._Execution._fetch(0x00400030));
		_ex.run_over();
		_ex.exit_output( "0x00400030", "exit");
		expectedOutput.append(log); log.clear();
		expectedOutput.append( "\n" ).append( EX_COMPLETE );
		
		compareWithSystemOut(expectedOutput);
	}
	
	@Test
	@DisplayName ( "Parse -> Assemble -> Execute => Error" )
	void Execute_Error ( ) {
		// Ex error, Loading from non Valid data Addr
		// Setup
		HashMap<Integer, Double> data = new HashMap<>();
		ExecutionLog log = new ExecutionLog( new ArrayList<>() );
		FMT_MSG._Execution _ex = new FMT_MSG._Execution( new int[32], data, log, log);
		//Parse->Assemble->Execute
		Main.main( new String[] { TEST_RESOURCES_DIR + "_Execution_Error.s" } );
		//Output
		StringBuilder expectedOutput=new StringBuilder();
		expectedOutput.append( PARSE_COMPLETE );
		expectedOutput.append( ASSEMBLE_COMPLETE );
		
		expectedOutput.append(
				"-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n" +
				"|R0: 0\tR4: 0\tR8: 0\tR12: 0\t\tR16: 0\tR20: 0\tR24: 0\tR28: 0|\n" +
				"|R1: 0\tR5: 0\tR9: 0\tR13: 0\t\tR17: 0\tR21: 0\tR25: 0\tR29: 0|\n" +
				"|R2: 0\tR6: 0\tR10: 0\tR14: 0\t\tR18: 0\tR22: 0\tR26: 0\tR30: 0|\n" +
				"|R3: 0\tR7: 0\tR11: 0\tR15: 0\t\tR19: 0\tR23: 0\tR27: 0\tR31: 0|\n" +
				"-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n\n"
		);
		// Line1 [0x00400000] SW R1, -20(R2)
		log.append(FMT_MSG._Execution._fetch(0x00400000));
		_ex.decode( "0x00400000", "sw", "IMMEDIATE" );
		_ex.rb_read( 0, 2 );
		_ex.rb_read( 0, 1 );
		_ex.imm_cal_addr( -20, 0, -20 );
		expectedOutput.append(log); log.clear();
		expectedOutput.append( "ERROR: Data Address [0xFFFFFFEC, -20] Must Be >=0x10010000 and <=0x100107F8!" );
		expectedOutput.append( "\n" ).append( END_WITH_ERRORS );
		compareWithSystemOut(expectedOutput);
	}
	
	@Test
	@DisplayName ( "Parse -> Assemble => Error" )
	void Assembly_Error ( ) {
		// Labels pointing to wrong destination
		// data <-> Instr
		// Target Label not defined.
		// Ex error not caught
		// Setup
		ExecutionLog ignored = new ExecutionLog( new ArrayList<>() );
		FMT_MSG._Execution _ex = new FMT_MSG._Execution( new int[32], new HashMap<>(), ignored, ignored);
		ErrorLog errors = new ErrorLog( new ArrayList<>() );
		//Parse->Assemble->Execute
		Main.main( new String[] { TEST_RESOURCES_DIR + "_Assembly_Error.s" } );
		//Output
		StringBuilder expectedOutput=new StringBuilder();
		expectedOutput.append( PARSE_COMPLETE );
		errors.appendEx( FMT_MSG.xAddressNot( "Instruction", "0x10010000", "Valid"));
		errors.appendEx( FMT_MSG.label.points2Invalid_Address( "x", "Instruction" ));
		errors.appendEx( FMT_MSG.label.labelNotFound( "y" ) );
		errors.appendEx( FMT_MSG.FailedAssemble );
		expectedOutput.append( errors.toString() );
		compareWithSystemOut(expectedOutput);
	}
	
	@Test
	@DisplayName ( "Parse  => Error" )
	void Parse_Error ( ) {
		// Floating Point Data
		// Assembly error not caught
		// Setup
		ExecutionLog log = new ExecutionLog( new ArrayList<>() );
		FMT_MSG._Execution _ex = new FMT_MSG._Execution( new int[32], new HashMap<>(), log, log);
		ErrorLog errors = new ErrorLog( new ArrayList<>() );
		//Parse->Assemble->Execute
		Main.main( new String[] { TEST_RESOURCES_DIR + "_Parse_Error.s" } );
		StringBuilder expectedOutput=new StringBuilder();
		errors.appendEx( FMT_MSG.xAddressNot( "Instruction",  "0x00000010",  "Valid"));
		errors.append( 1, FMT_MSG._opsForOpcodeNotValid("j","0x04"  ) );
		errors.appendEx( FMT_MSG.xAddressNot( "Data",  "0x00000168",  "Valid"));
		errors.append( 3, FMT_MSG._opsForOpcodeNotValid("lw","$1, 90"  ) );
		expectedOutput.append( errors.toString() );
		compareWithSystemOut(expectedOutput);
	}
	
}
