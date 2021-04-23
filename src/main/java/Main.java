import control.Execution;

import model.instr.Instruction;
import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.RegisterBank;

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
		final StringBuilder output = new StringBuilder();
		final String path=(args.length>0)?args[0]:"";
		
		// Parsing
		Parser parser=new Parser( path, MEMORY_BUILDER, errorLog, warningsLog );
		if ( errorLog.hasEntries( ) ) {
			warningsLog.println( );
		} else {
			System.out.println( "Parsing Complete!" );
			// Assemble
			ArrayList<Instruction> instructions=MEMORY_BUILDER.assembleInstr( errorLog );
			if ( instructions!=null ) {
				System.out.println( "Assembly Complete!" );
				//Execution
				// Setup Components
				ExecutionLog executionLog=new ExecutionLog( new ArrayList<>( ) );
				DataMemory dm=parser.getMem( executionLog );
				RegisterBank rb=new RegisterBank( new int[ 32 ], executionLog );
				// Execution
				Execution.RunToEnd( dm, rb, instructions, executionLog, output );
				
				// Output
				// Print Results
				System.out.println( output.toString( ) );
				if (!output.toString().contains( "ERROR" ))
					System.out.println( "Execution Complete!" );
				else
					System.out.println( "Execution Ended With Errors!" );
			}
		}
		errorLog.println();
	}
	
}
