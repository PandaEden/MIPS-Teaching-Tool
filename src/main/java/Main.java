import control.Execution;

import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Instruction;

import setup.Parser;

import util.Util;
import util.ansi_codes.Color;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.WarningsLog;

import java.util.ArrayList;

public class Main {
	private static final String ENTER ="'" + Color.fmtCmd( "ENTER" ) + "'";
	
	public static void main(String[] args) {
		//Disable Colour for Windows Terminals
		if ( System.console( )!=null && System.getenv( ).get( "TERM" )==null )
			Color.colorSupport=false;	// Tested Manually, enabled on CMD/Powershell
		//Setup
		final ErrorLog errorLog=new ErrorLog( new ArrayList<>( ) );
		final WarningsLog warningsLog=new WarningsLog( new ArrayList<>( ) );
		final MemoryBuilder MEMORY_BUILDER=new MemoryBuilder( errorLog, warningsLog );
		String path=(args.length>0)?args[0]:"";
		
		run( errorLog, warningsLog, MEMORY_BUILDER, path );
		if ( Util.wait ) {
			final String FILENAME = Color.fmtCmd("Path\\FileName");
			boolean exit=false;
			while ( !exit ){
				path=Util.input( "\nEnter another "+FILENAME+" to Run again,\n\tOr Press "+ENTER+" to Close the Application..." );
				if ( !Util.isNullOrBlank( path ) )
					run( errorLog, warningsLog, MEMORY_BUILDER, path );
				else
					exit=true;
			}
		}
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
				System.out.println( "Assembly Complete!\n" );
				//Execution
				// Setup Components
				ExecutionLog executionLog=new ExecutionLog( new ArrayList<>( ) );
				DataMemory dm=parser.getMem( executionLog );
				RegisterBank rb=new RegisterBank( new int[ 32 ], executionLog );
				// Execution
				Execution ex = new Execution( executionLog,errorLog, dm, rb, instructions );
				
				boolean exit = false;
				int n=1;
				final String NUMBER = Color.fmtCmd("Number");
				while ( !exit ){
					if ( Util.wait ) {
						String line = Util.input( "Enter a "+NUMBER+" to change the number of cycles to run"
												 +"\n\tPress "+ENTER+" to Run " +((n==1)?"a Cycle":"the next "+n+" Cycles")+". . .");
						// Attempt to find Integer
						try {
							if ( line.toLowerCase().equals( "end" ) )
								n=1000;
							else {
								n=Integer.parseInt( line );
								if ( n<1 )
									n=1;
								else if ( n>1000 )
									n=1000;
							}
						}catch ( NumberFormatException e ){
							// n=1
						}
					}
					
					StringBuilder out = new StringBuilder();
					exit = (ex.runSteps( out, n )==null);
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
