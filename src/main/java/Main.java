import control.Execution;

import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Instruction;

import setup.Parser;

import util.ansi_codes.Color;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.WarningsLog;

import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		//Disable Colour for Windows Terminals
		if ( System.console( )!=null && System.getenv( ).get( "TERM" )==null )
			Color.colorSupport=false;	// Tested Manually, enabled on CMD/Powershell
		//Setup
		final ErrorLog errorLog=new ErrorLog( new ArrayList<>( ) );
		final WarningsLog warningsLog=new WarningsLog( new ArrayList<>( ) );
		final MemoryBuilder MEMORY_BUILDER=new MemoryBuilder( errorLog, warningsLog );
		final String path=(args.length>0)?args[0]:"";
		
		run( errorLog, warningsLog, MEMORY_BUILDER, path );
	}
	
	public static void run(final ErrorLog errorLog, final WarningsLog warningsLog, final MemoryBuilder Memory, String path){
		// Parsing
		Parser parser=new Parser( path, Memory, errorLog, warningsLog );
		if ( errorLog.hasEntries( ) ) {
			warningsLog.println( );
		} else {
			System.out.println( "Parsing Complete!" );
			// Assemble
			ArrayList<Instruction> instructions=Memory.assembleInstr( errorLog );
			if ( instructions!=null ) {
				System.out.println( "Assembly Complete!" );
				//Execution
				// Setup Components
				ExecutionLog executionLog=new ExecutionLog( new ArrayList<>( ) );
				DataMemory dm=parser.getMem( executionLog );
				RegisterBank rb=new RegisterBank( new int[ 32 ], executionLog );
				// Execution
				Execution ex = new Execution( executionLog,errorLog, dm, rb, instructions );
				
				boolean exit = false;
				
				while ( !exit ){
					StringBuilder out = new StringBuilder();
					exit = (ex.runStep( out )==null);
					System.out.print( out );
				}
				
				// Output
				// Print Results
				if (!errorLog.hasEntries())
					System.out.println( "\nExecution Complete!" );
				else
					System.out.println( "\nExecution Ended With Errors!" );
			}
		}
		errorLog.println();
	}
	
}
