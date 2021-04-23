package setup;

import _test.TestLogs;
import _test.TestLogs.FMT_MSG;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import control.Execution;

import model.instr.Instruction;
import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;

import util.Convert;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
	private static final String TEST_RESOURCES_DIR="src" + File.separator + "test" + File.separator + "resources" + File.separator;
	
	private static Parser parser;
	private static MemoryBuilder mb;
	private static TestLogs testLogs;
	private static ErrorLog expected;
	
	@BeforeAll
	static void beforeAll ( ) {
		testLogs=new TestLogs( );
		expected=testLogs.expectedErrors;
		
		mb=new MemoryBuilder( testLogs.actualErrors, testLogs.actualWarnings );
		parser=new Parser( mb, testLogs.actualErrors, testLogs.actualWarnings );
	}
	
	@AfterEach
	void tearDown ( ) {
		testLogs.after( );
	}
	private void nullObjectErrors (Object object, String errorFormat) {
		assertNull( object );
		expected.appendEx( errorFormat );
	}
	
	private void assembleNoInstructions ( ) {
		nullObjectErrors( parser.assemble( ), "No Instructions Found" );
	}
	
	@Test
	@DisplayName ( "static variables are correct" )
	void staticVars ( ) {
		assertAll(    // Alt+F7 : Find Usages
					  //Data
					  ( ) -> assertEquals( 512, Parser.MAX_LINES ),
		
					  ( ) -> assertEquals( 256, DataMemory.MAX_DATA_ITEMS ),
					  ( ) -> assertEquals( 8, DataMemory.DATA_ALIGN ),
					  ( ) -> assertEquals( 268500992, DataMemory.BASE_DATA_ADDRESS ),
					  ( ) -> assertEquals( 268503040, DataMemory.OVER_SUPPORTED_DATA_ADDRESS ),
					  ( ) -> assertEquals( 268697600, DataMemory.OVER_DATA_ADDRESS ),
		
					  ( ) -> assertEquals( 256, InstrMemory.MAX_INSTR_COUNT ),
					  ( ) -> assertEquals( 4, InstrMemory.ADDR_SIZE ),
					  ( ) -> assertEquals( 4194304, InstrMemory.BASE_INSTR_ADDRESS ),
					  ( ) -> assertEquals( 5242884, InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS ),
					  ( ) -> assertEquals( 268435456, InstrMemory.OVER_INSTR_ADDRESS )
		);
	}
	
	// Parsing
	@Test
	void Invalid_Directive ( ) {
		assertFalse( parser.parseLine( "  .nonMipsDirective ", -5 ) );
		
		expected.appendEx( -5, FMT_MSG.DirectiveNotSupported( ".nonMipsDirective".toLowerCase( ) ) );
		assembleNoInstructions( );
	}
	
	@ParameterizedTest ( name="[{index}] Parse Valid Directive[{arguments}]" )
	@ValueSource ( strings={ ".data", ".text", ".code" } )
	void Valid_Directive (String directive) {
		parser.parseLine( directive, -5 );
		
		assertTrue( parser.parseLine( directive, -5 ) );
		assembleNoInstructions( );
	}
	
	@Nested
	@DisplayName ( "File Tests" )
	class FileTests {
		
		@Test
		@DisplayName ( "File Does Not Exist!" )
		void fileDoesNotExist ( ) {
			File temp=new File( TEST_RESOURCES_DIR + "Not_Actual_File" );
			assertFalse( temp.exists( ) ); // If the File exists, test is invalid
			
			nullObjectErrors( parser.loadFile( TEST_RESOURCES_DIR + "Not_Actual_File" ),
							  "File: \"Not_Actual_File\", Does Not Exist" );
		}
		
		@Test
		@DisplayName ( "Default Filename For Blank FileName" )
		void defaultFilenameForBlankFileName ( ) {
			assertEquals( "FileInput.s", parser.loadFile( " " ).getName( ) );
			testLogs.expectedWarnings.append( "Filename Not Provided, Using Default File: \"FileInput.s\"" );
		}
		
		@SuppressWarnings ( "SpellCheckingInspection" )
		@Test
		@DisplayName ( "File Over MAX Lines" )
		void fileOverMaxLines ( ) {
			assertFalse( parser.parseFile( parser.loadFile( TEST_RESOURCES_DIR + "FileOver30Klines.s" ) ) );
			expected.appendEx( "File: \"FileOver30Klines.s\", Has Too Many Lines!, Max Lines = [512]" );
		}
		
		@ParameterizedTest ( name="[{index}] FileName[{arguments}] Not Valid" )
		@EnabledOnOs ( org.junit.jupiter.api.condition.OS.WINDOWS )
		@ValueSource ( strings={ "file|.s", "fi?le.s", "file.a*m" } )
		void Invalid_FileName (String text) {
			nullObjectErrors( parser.loadFile( text ),
							  "File: \"" + text + "\", Not Valid FileName" );
		}
		
		@Test
		@Disabled	// Manually Tested on Ubuntu (WSL)
		@DisplayName ( "File Not Accessible /NotReadable" )
		void File_NotAccessible ( ) {
			// Tried creating a file on the system without Read Permission, This made it think it was a non-valid file?
			// Run test if platform is not Windows
			if ( !System.getProperty( "os.name" ).toLowerCase( ).contains( "win" ) ) {
				
				File tempFile=null;
				try {
					tempFile=File.createTempFile( "Busy-File", ".s" );
					String filename=tempFile.getPath( );
					
					if ( tempFile.setReadable( false ) )    // if successfully made file unReadable
						nullObjectErrors( parser.loadFile( filename ),
										  "File: \"" + filename + "\", Not Readable!" );
					else
						fail( "Cannot Test Unreadable File" );
					
				} catch ( IOException e ) {
					e.printStackTrace( );
				} finally {
					assert tempFile!=null;
					tempFile.deleteOnExit( );
				}
			}
		}
		
		@Test
		void Null_File ( ) {
			assertFalse( parser.parseFile( null ) );
		}
		
	}
	
	@Nested
			// TODO - Refactor To Parametrized
	class Split_Line_Test {
		private final List<String> comments=Arrays.asList(
				"   #  HashOnly_  Text    1233 addi $r0   ",
				"   ;  SemiOnly_ Random Text 2",
				"#   HashFirst_ Random ;Text  ",
				"   ;  SemiFirst_ Random #Text  "
		);
		private final List<String> labels=Arrays.asList( "panda :   ", "_notAPanda:", "  :", " not a valid label :" );
		
		@Test
		void Split_CommentOnly ( ) {
			for ( String comment : comments ) {
				String[] split=parser.splitLine( comment );
				assertNull( split[ 0 ] );
				assertNull( split[ 1 ] );
				assertNull( split[ 2 ] );
				assertEquals( Convert.removeExtraWhitespace( comment ), split[ 3 ] ); // extra whitespace is trimmed
			}
		}
		
		@Test
		void Split_LabelsOnly ( ) {
			for ( String label : labels ) {
				String[] split=parser.splitLine( label );
				assertEquals( Convert.removeExtraWhitespace( label ).split( ":", 2 )[ 0 ].toLowerCase( ), split[ 0 ] );
				assertNull( split[ 1 ] );
				assertNull( split[ 2 ] );
				assertNull( split[ 3 ] );
			}
		}
		
		@Test
		void Split_Labels_And_Comments ( ) {
			for ( String comment : comments ) {
				for ( String label : labels ) {
					String[] split=parser.splitLine( label + comment );
					assertEquals( Convert.removeExtraWhitespace( label ).split( ":", 2 )[ 0 ].toLowerCase( ), split[ 0 ] );
					assertNull( split[ 1 ] );
					assertNull( split[ 2 ] );
					assertEquals( Convert.removeExtraWhitespace( comment ), split[ 3 ] ); // extra whitespace is trimmed
				}
			}
		}
		
		@ParameterizedTest ( name="[{index}] Splitting Argument1 [{arguments}]" )
		@ValueSource ( strings={ "  add  ", ".data  ", ".word", "l.d", "  ct.l.d", "PANDA" } )
		void Split_Arg1_Only (String arg) {
			String[] split=parser.splitLine( arg );
			assertNull( split[ 0 ] );
			assertEquals( arg.strip( ).toLowerCase( ), split[ 1 ] ); // extra whitespace is trimmed
			assertNull( split[ 2 ] );
			assertNull( split[ 3 ] );
		}
		
		@Test
		void Split_Arg_1_And_2_Only ( ) {
			//TODO - revise Parsing mechanism to better determine labels
			String[] split=parser.splitLine( " ADDI    $r0,  .  87  NOT VA:LID   " );
			assertEquals( "addi $r0, . 87 not va", split[ 0 ] );
			assertEquals( "lid", split[ 1 ] );
			assertNull( split[ 2 ] );
			assertNull( split[ 3 ] );
			
			//TODO: atm, it thinks everything unto ':' is part of a label. It does not check if the first space separated word
			//  is a valid directive/ opcode.   (labels can't start with a dot. and )
			//assertEquals( "addi", split[ 1 ] );
			//assertEquals( "$r0, . 87 not va:lid", split[ 2 ] );
		}
		
		@Test
		void Split_Complete ( ) {
			String line="	_LAB.3L:\tADD.d \tR0 5:6,	0x59 ; s:e  m.:i  # Comments    Section!  ";
			String[] split=parser.splitLine( line );
			assertEquals( "_lab.3l", split[ 0 ] );
			assertEquals( "add.d", split[ 1 ] );
			assertEquals( "r0 5:6, 0x59", split[ 2 ] );
			assertEquals( "; s:e m.:i # Comments Section!", split[ 3 ] );
		}
		
		@Test
		void Split_Blank ( ) {
			String[] split=parser.splitLine( "   " );
			assertNull( split[ 0 ] );
			assertNull( split[ 1 ] );
			assertNull( split[ 2 ] );
			assertNull( split[ 3 ] );
		}
		
	}
	
	@Nested
	class Parse_Convert_Invalid_Lines {
		@BeforeEach
		void setUp ( ) {
			parser.clear( );
		}
		@Test
		void over_Max_Instructions ( ) {
			String FilePath=TEST_RESOURCES_DIR + "Parse_FileOver256Instructions.s";
			
			assertTrue( parser.parseFile( parser.loadFile( FilePath ) ) );
			testLogs.expectedWarnings.appendEx( 257, "Reached MAX Instructions!, Further Instructions Will Not Be Parsed" );
			testLogs.expectedWarnings.append( "\t\t\tInstruction Limit == [256]" );
		}
		
		@Test
		void Over_Max_Data ( ) {
			String FilePath=TEST_RESOURCES_DIR + "Parse_FileOver256DataIndexes.s";
			
			assertTrue( parser.parseFile( parser.loadFile( FilePath ) ) );
			testLogs.expectedWarnings.appendEx( 3, "Reached MAX Data Size!, No More Data Will Be Parsed" );
			testLogs.expectedWarnings.append( "\t\t\tData Limit == [256]" );
		}
		
		@Test
		void Parse_InvalidLines_Label_Opcode ( ) {
			String line="  p a n   d a : addi r-8, R70, 32.76 ; # comment";
			
			assertFalse( parser.parseLine( line, -80 ) );
			
			expected.appendEx( -80, FMT_MSG.label.notSupp( "p a n d a " ) );
			//expected.append( "_");
			testLogs.appendErrors( -80,
								   FMT_MSG.reg._NotRecognised( "r-8" ),
								   FMT_MSG.reg._NotRecognised( "r70" ),
								   FMT_MSG.imm.notValInt( "32.76" ) + "!",
								   FMT_MSG._opsForOpcodeNotValid( "addi", "r-8, r70, 32.76" ) );
		}
		
		@ParameterizedTest
		@ValueSource ( strings={ "j ", ".word -5:-5", ".word 8589934592", "add R0, R0", "SUB R0 R0 R0",
								 "j 0", "j 0x 0040000", "JAL 0x140001", "lw 0x00400000", "lw 20 (R0)", "sw 50 ( 76)", ".word " } )
		@DisplayName ( "Test Parse InvalidLines" )
		void Parse_Invalid_Lines (String line) {
			assertFalse( parser.parseLine( line, 5 ) );
			assertTrue( testLogs.actualErrors.hasEntries( ) );
			
			testLogs.actualErrors.clear( ); // Errors Vary too much to check individually
		}
		
		@Test
		void Parse_Invalid_File ( ) {
			assertFalse( parser.parseFile( new File( "Invalid-Name" ) ) );
			expected.appendEx( "File: \"Invalid-Name\", Not Valid FileName - Parsed" );
		}
		
	}
	
	@SuppressWarnings ( "SpellCheckingInspection" )
	@Test
	void ParseFile_With_Errors ( ) {
		assertFalse( parser.loadParseFile( TEST_RESOURCES_DIR + "Parse_Invalid.s" ) );
		expected.appendEx( 3, FMT_MSG.DirectiveNotSupported( ".word5" ) );
		expected.appendEx( 4, FMT_MSG.DirectiveNotSupported( ".woasd" ) );
		expected.appendEx( FMT_MSG.data.NotValSignedInt( "7,9" ) );
		// TODO Add a Warning for Data not specified after .data directive
		expected.appendEx( 8, FMT_MSG.Opcode_NotSupported( "asdikhbjkbjfkbjsdyu" ) );
		expected.appendEx( 9, FMT_MSG.Opcode_NotSupported( "1238989hwedhuwqd7823" ) );
		expected.appendEx( 10, FMT_MSG.label.notSupp( "1line" ) );
		
		expected.appendEx( 12, FMT_MSG.Opcode_NotSupported( "and" ) );
		testLogs.zeroWarning( 13, "$0" );
		expected.appendEx( 15, FMT_MSG.Opcode_NotSupported( "panda" ) );
		
		expected.appendEx( 17, FMT_MSG.DirectiveNotSupported( ".label:" ) );
		testLogs.appendErrors( 18, FMT_MSG.label.notSupp( ".label" ) + "!",
							   FMT_MSG._opsForOpcodeNotValid( "j", ".label" ) );
		// TODO - Duplicate Label Warning - line 19
		testLogs.appendErrors( 21, FMT_MSG.label.notSupp( "val_label:" ) + "!",
							   FMT_MSG._opsForOpcodeNotValid( "j", "val_label:" ) );
		expected.append( 24, FMT_MSG._opsForOpcodeNotValid( "lw", "r0 0x10010008" ) );
		testLogs.zeroWarning( 25, "r0" ); // Lowercase
		// Assembly Error - Points to Invalid Data Addr
		
		testLogs.appendErrors( 28, FMT_MSG.label.notSupp( "p a n d a" ) + "!",
							   FMT_MSG.reg._NotRecognised( "5" ),
							   FMT_MSG.reg._NotRecognised( "r70" ),
							   FMT_MSG.imm.notSigned16Bit( 32768 ) + "!",
							   FMT_MSG._opsForOpcodeNotValid( "addi", "5, r70, 32768" ) );
		// line 33, invalid data address - caught at assembly
		testLogs.appendErrors( 34, FMT_MSG.imm.notSigned16Bit( -32769 ) + "!",
							   FMT_MSG._opsForOpcodeNotValid( "sw", "$1, -32769" ) );
		
		testLogs.appendErrors( 36, FMT_MSG.imm.notValInt( "8589934592" ) + "!",
							   FMT_MSG._opsForOpcodeNotValid( "j", "8589934592" ) );
		
		testLogs.appendErrors( 38, FMT_MSG._NO_OPS,
							   FMT_MSG._opsForOpcodeNotValid( "j", null ) );
		testLogs.appendErrors( 39, FMT_MSG._opsForOpcodeNotValid( "exit", "r0" ) );
	}
	
	@Test
	void Parse_Successfully_And_Retrieved_Model ( ) {
		assertTrue( parser.loadParseFile( TEST_RESOURCES_DIR + "Execution_NoBranches.s" ) );
		ExecutionLog ignored =  new ExecutionLog( new ArrayList<>( ) );
		
		ArrayList<Instruction> ins=mb.assembleInstr( testLogs.actualErrors );
		// Not testing the output of accessing the data here
		DataMemory dataMemory=parser.getMem(ignored );
		int[] values = new int[32];
		// Data
		int addr=0x10010000;
		assertEquals( 50, dataMemory.readData( addr ) );
		assertEquals( 268500992, dataMemory.readData( addr + 8 ) );
		for ( int i=2; i<82; i++ ) {
			assertEquals( -3, dataMemory.readData( addr + (i*8) ) );
		}
		assertEquals( -900, dataMemory.readData( addr + 0x290 ) ); // index 82
		for ( int i=83; i<256; i++ ) {
			assertEquals( 0, dataMemory.readData( addr + (i*8) ) );
		}
		assertEquals( 12, ins.size( ) );
		// TODO ToString not implemented in Instruction, so I can't check the order of instructions
		
		StringBuilder output = new StringBuilder();
		Execution.RunToEnd( dataMemory, new RegisterBank( values, ignored ), ins, ignored, output);
		
		// Post Execution Results
		
		// Check values of RegisterBank are as expected
		for ( int i=0; i<8; i++ ) {
			assertEquals(0, values[i]);
		}
		assertEquals(268500992, values[8]);
		assertEquals(0, values[9]);
		assertEquals(268501008, values[10]);
		for ( int i=11; i<16; i++ ) {
			assertEquals(0, values[i]);
		}
		assertEquals(50, values[16]);
		assertEquals(0, values[17]);
		assertEquals(100, values[18]);
		assertEquals(0, values[19]);
		assertEquals(-800, values[20]);
		assertEquals(0, values[21]);
		assertEquals(-900, values[22]);
		assertEquals(0, values[23]);
		assertEquals(-950, values[24]);
		assertEquals(0, values[25]);
		for ( int i=26; i<32; i++ ) {
			assertEquals(0, values[i]);
		}
		// Check Data Values are as expected
		addr=0x10010000;
		assertEquals( 50, dataMemory.readData( addr ) );	// Unchanged
		assertEquals( 268500992, dataMemory.readData( addr + 8 ) );	// Unchanged
		assertEquals( -900, dataMemory.readData( addr + (2*8) ) ); //<- Modified
		for ( int i=3; i<82; i++ ) {
			assertEquals( -3, dataMemory.readData( addr + (i*8) ) ); // 3-81 Unchanged
		}
		assertEquals( -900, dataMemory.readData( addr + (82*8) ) ); // Unchanged
		for ( int i=83; i<256; i++ ) {
			assertEquals( 0, dataMemory.readData( addr + (i*8) ) ); // Unchanged
		}
	}
	
}
