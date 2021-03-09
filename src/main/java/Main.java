import model.Instruction;
import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;
import org.jetbrains.annotations.Nullable;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.ArrayList;
import java.util.HashMap;
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
			execute(instructions);
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
	
	private static void execute(ArrayList<Instruction> instructions){
		ExecutionLog exLog = new ExecutionLog(new ArrayList<>());
		DataMemory dataMem = new DataMemory(new HashMap<>(), exLog);
		RegisterBank regBank = new RegisterBank(new int[32], exLog);
		InstrMemory instrMemory = new InstrMemory(instructions, exLog);
		
		execute(dataMem, regBank, instrMemory, exLog);
		exLog.println();
	}
	
	public static void execute(DataMemory dataMem, RegisterBank regBank, InstrMemory instrMemory, ExecutionLog exLog){
		//
		Instruction ins;
		for (Integer PC = InstrMemory.BASE_INSTR_ADDRESS;
			 PC!=null;
			 PC = ins.execute(PC, dataMem, regBank, exLog)) {
			exLog.println();exLog.clear(); // print ExecutionLog
			System.out.print(regBank.format()); // print RegisterBank status
			ins = instrMemory.InstructionFetch(PC);
		}
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