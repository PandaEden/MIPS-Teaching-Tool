package test_util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.components.DataMemory;
import model.components.InstrMemory;

import setup.Parser;

import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 Shared Testing Variables/ Methods.
 <p>
 Methods automatically clears the logs, so further tests do not also fail.
 */
public class Var {
	
	public final ErrorLog errorLog=new ErrorLog( new ArrayList<>( ) );
	public final WarningsLog warningsLog=new WarningsLog( new ArrayList<>( ) );
	public final ExecutionLog executionLog=new ExecutionLog( new ArrayList<>( ) );
	
	/**
	 Automatically clears the logs if they are not empty, so further tests do not also fail.
	 */
	public void assertLogsAreEmpty() {
		TestMethods.noEntries( errorLog );
		TestMethods.noEntries( warningsLog );
		TestMethods.noEntries( executionLog );
	}
	
	/** Auto Prefix "Error:\n\t", and "\n" Suffix */
	public void errorMatches(String msg) {
		TestMethods.entriesMatch( errorLog, msg );
	}
	
	/** Auto Prefix "Error:\n\tLineNo: [lineNo]\t", and "\n" Suffix */
	public void errorMatches(String msg, int lineNo) {
		TestMethods.entriesMatch( errorLog, "LineNo: " + lineNo + "\t"+msg );
	}
	public void errorsMatch(String[] list, int lineNo) {
		StringBuilder msg=new StringBuilder( );
		for (int i=0;i<list.length;i++ ){
			if ( i!=0 )
				msg.append( "\t" );
			
			msg.append( "LineNo: " ).append( lineNo ).append( "\t" ).append( list[i] );
			
			if ( i!=list.length-1 )
				msg.append( "\n" );
		}
		TestMethods.entriesMatch( errorLog, msg.toString() );
	}
	public void zeroWarning(int lineNo, String regName) {
		TestMethods.entriesMatch( warningsLog, "LineNo: " + lineNo + "\t" + "Destination Register: \"" + regName
											   + "\" Cannot be modified!,\t Result will be ignored!" );
	}
	
	@Test
	@DisplayName ("static variables are correct")
	void staticVars() {
		Assertions.assertAll(    // Alt+F7 : Find Usages
								 //Data
								 () -> assertEquals( 512, Parser.MAX_LINES ),
		
								 () -> assertEquals( 256, DataMemory.MAX_DATA_ITEMS ),
								 () -> assertEquals( 8, DataMemory.DATA_ALIGN ),
								 () -> assertEquals( 268500992, DataMemory.BASE_DATA_ADDRESS ),
								 () -> assertEquals( 268503040, DataMemory.OVER_SUPPORTED_DATA_ADDRESS ),
								 () -> assertEquals( 268697600, DataMemory.OVER_DATA_ADDRESS ),
		
								 () -> assertEquals( 256, InstrMemory.MAX_INSTR_COUNT ),
								 () -> assertEquals( 4, InstrMemory.ADDR_SIZE ),
								 () -> assertEquals( 4194304, InstrMemory.BASE_INSTR_ADDRESS ),
								 () -> assertEquals( 5242880, InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS ),
								 () -> assertEquals( 268435456, InstrMemory.OVER_INSTR_ADDRESS )
		);
	}
	
	private static class TestMethods {
		private static String capture2StringThenClear(Logger log){
			String actual = log.toString( );
			log.clear( );    // Just In case it was not empty. Make it Empty so further tests do not also fail.
			return actual;
		}
		
		/**
		 Checks that Logger Contains No Entries. -> If it does, It prints them.
		 <b>After the test, the Logger is Cleared!</b>
		 */
		public static void noEntries(Logger log) {
			log.println( );    // Expected this to be empty, so when tests are passing it will not print anything.
			Assertions.assertAll(
					() -> assertFalse( log.hasEntries( ) ),
					() -> assertEquals( "", capture2StringThenClear(log) )
			);
		}
		
		/**
		 Checks the msg against the contents of Log.
		 <p>Automatically appends "{logMame}\n\t" Prefix, and "\n" Suffix</p>
		 <b>After the test, the Logger is Cleared!</b>
		 */
		public static void entriesMatch(Logger log, String msg) {
			Assertions.assertAll(
					() -> assertTrue( log.hasEntries( ) ),
					() -> assertEquals( log.getName( ) + ":\n\t" + msg + "\n", capture2StringThenClear(log) )
			);
		}
		
	}
}
