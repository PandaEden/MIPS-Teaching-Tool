import model.Instruction;
import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;
import util.Validate;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.WarningsLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main{
	private static final String InputFile = "FileInput.s";
	//private static ArrayList<Instruction> instructions;
	private static final ErrorLog errorLog = new ErrorLog(new ArrayList<>());
	private static final WarningsLog warningsLog = new WarningsLog(new ArrayList<>());
	private static final Validate validate = new Validate(errorLog);
	private static final MemoryBuilder MEMORY_BUILDER = new MemoryBuilder();
	
	private static ParseMode parseMode = ParseMode.TEXT;
	
	@SuppressWarnings ("unused")
	public static void parse(){
		int ins_index = 0;
		int lineNo = 1;
		// Setup: Parse file to lines
		try { // Try to read input file
			File input = new File(InputFile);
			Scanner reader = new Scanner(input);
			
			//Declare variables for loop
			String currentLine, ins, operands, comments = null;
			String[] split;
			
			while (reader.hasNextLine()) {
				//Get Next Line
				currentLine = reader.nextLine();
				ins = "no_ins";
				
				//Split Line around comment, first "#"
				if (currentLine.contains("#")) {
					split = currentLine.split(surOptSpace("#"), 2);
					
					currentLine = split[0];
					comments = "#"+split[1]; // append after #, so # is included in comment.
				}
				
				//Split line around Tag, first ":"
				if (currentLine.contains(":")) {
					//TODO , : matches Range - Data. <intVal>:<intN>
					split = currentLine.split(surOptSpace(":"), 2);
					
					MEMORY_BUILDER.pushLabel(split[0]);
					currentLine = split[1];
				}
				
				//SET_PARSE_MODE
				if (currentLine.toLowerCase().contains(".data")) {
					parseMode = ParseMode.DATA;
					continue;
				} else if (currentLine.toLowerCase().contains(".text")) {
					parseMode = ParseMode.TEXT;
					continue;
				}
				//Trim whitespace
				currentLine = currentLine.trim();
				
				if (parseMode==ParseMode.TEXT && !currentLine.isEmpty()) {
					//Split line around first space, ins" "$first_operand
					if (currentLine.contains(" ")) {
						split = currentLine.split(" ", 2);
						
						ins = split[0];
						currentLine = split[1];//Remainder should just be operands
					} else
						ins = currentLine;
					
					//remainder is just operands (comma and space ", " separated)
					operands = currentLine.strip();
					
					if (!ins.equals("no_ins")) {
						MEMORY_BUILDER.addInstruction(ins,
								validate.splitValidOperands(lineNo, ins, operands, warningsLog));
					}
				} else if (parseMode==ParseMode.DATA && currentLine.contains(".")) {
					try {
						String[] data = currentLine.split(" ", 2);
						MEMORY_BUILDER.addData(data[0], data[1], errorLog);
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
				lineNo++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static String surOptSpace(String regex){
		return "\\s?"+regex+"\\s?";
	}
	
	public static void main(String[] args){
		parse();
		System.out.println("Setup Finished\n");
		// Memory -> Memory Builder
		// Assemble -> Returns Instructions <>
		// InstrMem <--
		// InstrMem.fetch(PC) ; PC = NPC ;
		// NPC <- Execute(PC);
		
		// Assemble & Fetch Instructions
		ArrayList<Instruction> instructions = MEMORY_BUILDER.assembleInstr(errorLog);
		if (instructions!=null) {
			execute(instructions);
		}
		System.out.println(errorLog.toString());
	}
	
	private static void execute(ArrayList<Instruction> instructions){
		ExecutionLog exLog = new ExecutionLog(new ArrayList<>());
		DataMemory dataMem = new DataMemory(new HashMap<>(), exLog);
		RegisterBank regBank = new RegisterBank(new int[32], exLog);
		InstrMemory instrMemory = new InstrMemory(instructions, exLog);
		
		//
		Instruction ins;
		for (Integer PC = InstrMemory.BASE_INSTR_ADDRESS; PC!=null; PC = ins.execute(PC, dataMem, regBank, exLog)) {
			System.out.println(exLog.toString()); // print ExecutionLog
			System.out.print(regBank.format()); // print RegisterBank status
			ins = instrMemory.InstructionFetch(PC);
		}
	}
	
	private enum ParseMode{DATA, TEXT}
}