import org.jetbrains.annotations.Nullable;
import control.Execute;

import model.Instruction;
import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;

import setup.Parser;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.ArrayList;
import java.util.Scanner;

public class Main{
	//private static ArrayList<Instruction> instructions;
	private static final ErrorLog errorLog = new ErrorLog(new ArrayList<>());
	private static final WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
	private static final MemoryBuilder MEMORY_BUILDER = new MemoryBuilder();
	
	public static void main(String[] args){
		Logger.Color.colorSupport=true;
		Parser p = new Parser(MEMORY_BUILDER, errorLog, warningsLog);
		System.out.println("Parsing Finished\n");
		warningsLog.println();
		waitForInput(null);
		//// ASSEMBLE
		ArrayList<Instruction> instructions = MEMORY_BUILDER.assembleInstr(errorLog);
		if (instructions==null){
			errorLog.println();
			System.out.println("xyz");
		}else{
			// Setup Components
			ExecutionLog executionLog=new ExecutionLog( new ArrayList<>( ) );
			DataMemory dm=new DataMemory( MEMORY_BUILDER.retrieveData( ), executionLog );
			RegisterBank rb=new RegisterBank( new int[ 32 ], executionLog );
			InstrMemory im=new InstrMemory( instructions, executionLog );
			
			// Execute
			Execute.execute( dm, rb, im, executionLog );
			
			// Print Results
			Logger.Color.colorSupport=true;
			errorLog.println();
			warningsLog.println();
		}
		
		// Memory -> Memory Builder
		// Assemble -> Returns Instructions <>
		// InstrMem <--
		// InstrMem.fetch(PC) ; PC = NPC ;
		// NPC <- Execute(PC);
	}
	
	@SuppressWarnings ("UnusedReturnValue")
	public static String waitForInput(@Nullable String msg){
		if (msg==null){
			msg = "Press ENTER to continue . . .";
		}
		System.out.print(Logger.Color.formatColored(Logger.Color.WHITE_ANSI, msg));
		return new Scanner(System.in).nextLine();
	}
}