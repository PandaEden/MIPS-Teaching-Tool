package util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import _test.Tags;
import _test.Tags.Pkg;

import _test.providers.BlankProvider;

import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@Tag ( Pkg.UTIL )
@Tag ( Tags.OUT )
@DisplayName ( Pkg.UTIL+" : Logger Test" )
class LoggerTest {
	private Logger logger;
	private ArrayList<String> logs;
	
	@BeforeEach
	void setUp() {
		logs=new ArrayList<>( );
		logger=new Logger( "Test", logs );
	}
	
	@Nested
	@Tag(Tags.MUT)
	class Append {
		@Test
		void append() {
			logger.append( "err1" );
			logger.append( "err2" );
			//noinspection ResultOfMethodCallIgnored
			Assertions.assertAll(
					() -> assertTrue( logger.hasEntries( ) ),
					() -> assertEquals( "Test:\n\terr1\n\terr2\n", logger.toString( ) ),
					() -> assertEquals( "err1", logs.get( 0 ) ),
					() -> assertThrows( IndexOutOfBoundsException.class, () -> logs.get( 2 ) ),
					() -> assertFalse( logs.isEmpty( ) )
			);
		}
		@Test
		void Append_ExclamationMark() {
			logger.appendEx( "err1 " );
			logger.appendEx( "err2 " );
			//noinspection ResultOfMethodCallIgnored
			Assertions.assertAll(
					() -> assertTrue( logger.hasEntries( ) ),
					() -> assertEquals( "Test:\n\terr1 !\n\terr2 !\n", logger.toString( ) ),
					() -> assertEquals( "err1 !", logs.get( 0 ) ),
					() -> assertThrows( IndexOutOfBoundsException.class, () -> logs.get( 2 ) ),
					() -> assertFalse( logs.isEmpty( ) )
			);
		}
		
		@Test
		void append_String_With_Internal_WhiteSpace() {
			logger.append( "something else    with spaces" );
			assertEquals( 1, logs.size( ) );
			Assertions.assertAll(
					() -> assertTrue( logger.hasEntries( ) ),
					() -> assertEquals( "Test:\n\tsomething else    with spaces\n", logger.toString( ) ),
					() -> assertEquals( "something else    with spaces", logs.get( 0 ) ),
					() -> assertFalse( logs.isEmpty( ) )
			);
		}
		
		@ParameterizedTest (name="Ignore Null or Blank[{index}] - Append: \"{0}\"")
		@ArgumentsSource( BlankProvider.NullNwLn.class )
		void append_Ignores_BlankOrNull(String blankText) {
			logger.append( blankText );
			assertTrue( logs.isEmpty( ) );    // adding Blank/Null shouldn't change the size
			
			logger.append( "something" );
			
			logger.append( blankText );
			assertEquals( 1, logs.size( ) );
		}
		
		@Test
		void append_Prefix_LineNo() {
			assertNull(logger.setPrefix( "Pre:" ));
			logger.append( "No LineNo" );
			assertEquals("Pre:", logger.setLineNoPrefix( 5 ));
			logger.appendEx( "\tThing" );
			logger.append( "Another Thing" );
			assertEquals("LineNo: 5", logger.clearPrefix());
			logger.append( "No Prefix" );
			logger.append( 20,"Another Thing" );	// Single Line Prefix LineNo
			logger.append( "No Prefix" );
			
			assertEquals( "Test:\n"
						  +"\tPre:\tNo LineNo\n"
						  +"\tLineNo: 5\t\tThing!\n"
						  +"\tLineNo: 5\tAnother Thing\n"
						  +"\tNo Prefix\n"
						  +"\tLineNo: 20\tAnother Thing\n"
						  +"\tNo Prefix\n",
						  logger.toString());
		}
		
	}
	
	@Test
	@Tag(Tags.MUT)
	void Clear_Logger() {
		logs.add( "panda" );
		assertTrue( logger.hasEntries( ) ); // Test Invalid if false
		
		logger.clear( );
		Assertions.assertAll(
				() -> assertFalse( logger.hasEntries( ) ),
				() -> assertEquals( "", logger.toString( ) ),
				() -> assertTrue( logs.isEmpty( ) )
		);
	}
	
	@Test
	@Tag (Tags.ACC)
	void Get_Name() {
		assertEquals( "Test", logger.getName( ) );
	}
	
	@Test
	@Tag ("Accessing")
	void System_Print() {
		// Setup - redirecting Standard Output
		final PrintStream standardOut=System.out;
		final ByteArrayOutputStream outputStreamCaptor=new ByteArrayOutputStream( );
		System.setOut( new PrintStream( outputStreamCaptor ) );
		logs.add( "SUPER-PANDA!" );
		
		logger.println( );
		assertEquals( "Test:\n\tSUPER-PANDA!\n", outputStreamCaptor.toString( ) );
		
		// Restore original System.Out
		System.setOut( standardOut );
	}
	
	@Test
	@DisplayName ("Test Logger Extensions (SubClasses)")
	void testSub_Logs() {
		logs.add( "panda" );
		logs.add( "another Panda" );
		
		logger=new ErrorLog( logs );
		//logs already contains {"err1", "err2"}
		Assertions.assertAll(
				() -> assertTrue( logger.hasEntries( ) ),
				() -> assertEquals( "Errors", logger.getName( ) ),
				() -> assertEquals( "Errors:\n\tpanda\n\tanother Panda\n", logger.toString( ) )
		);
		
		logger=new WarningsLog( logs );
		//logs already contains {"err1", "err2"}
		logger.append( "warn1" );
		Assertions.assertAll(
				() -> assertTrue( logger.hasEntries( ) ),
				() -> assertEquals( "Warnings", logger.getName( ) ),
				() -> assertEquals( "Warnings:\n\tpanda\n\tanother Panda\n\twarn1\n", logger.toString( ) )
		);
		
		logs.clear( );
		
		logger=new ExecutionLog( logs );
		//logs already contains {"err1", "err2"}
		logger.append( "instr1" );
		Assertions.assertAll(
				() -> assertTrue( logger.hasEntries( ) ),
				() -> assertEquals( "Execution", logger.getName( ) ),
				() -> assertEquals( "Execution:\n\tinstr1\n", logger.toString( ) )
		);
	}
	
	@Nested
	@Tag( "Color" )
	class Color_Support {
		boolean preset;
		
		@BeforeEach
		void setUp() {
			preset=Logger.Color.colorSupport;
			Logger.Color.colorSupport=true; // enable colour support
		}
		
		@AfterEach
		void tearDown() {
			Logger.Color.colorSupport=preset; // reset colour support
		}
		
		@ParameterizedTest (name="Color[{index}] - Ansi: \"{arguments}\"")
		@ValueSource (strings={
				Logger.Color.ANSI_RESET,
				Logger.Color.BLACK_ANSI,
				Logger.Color.RED_ANSI,
				Logger.Color.GREEN_ANSI,
				Logger.Color.YELLOW_ANSI,
				Logger.Color.BLUE_ANSI,
				Logger.Color.PURPLE_ANSI,
				Logger.Color.CYAN_ANSI,
				Logger.Color.WHITE_ANSI
		})

		void Format_Colored(String color) {
			System.out.println( Logger.Color.fmtColored( color, "<PANDA>" ) );
			assertEquals( color + "PANDA\u001B[0m", Logger.Color.fmtColored( color, "PANDA" ) );
		}
		
	}
	
}
