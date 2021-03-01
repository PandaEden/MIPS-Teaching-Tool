package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main{
	private static final String InputFile = "FileInput.s";
	//private static ArrayList<Instruction> instructions;
	private static final Memory memory = new Memory();
	private static ParseMode parseMode = ParseMode.TEXT;

	public static void setup(){
		int ins_index = 0;
		// Setup: Parse file to lines
		try { // Try to read input file
			File input = new File(InputFile);
			Scanner reader = new Scanner(input);
			
			//Declare variables for loop
			String currentLine, ins, comments = null;
			String[] operands, split;
			
			while (reader.hasNextLine()) {
				//Get Next Line
				currentLine = reader.nextLine();
				ins = "no_ins";
				
				//Split Line around comment, first "#"
				if (currentLine.contains("#")) {
					split = currentLine.split("#", 2);
					
					currentLine = split[0];
					comments = "#"+split[1]; // append after #, so # is included in comment.
				}
				
				//SET_PARSE_MODE
				if (currentLine.toLowerCase().contains(".data")) {
					parseMode = ParseMode.DATA;
					continue;
				} else if (currentLine.toLowerCase().contains(".text")) {
					parseMode = ParseMode.TEXT;
					continue;
				}
				//Split line around Tag, first ":"
				if (currentLine.contains(":")) {
					split = currentLine.split(":", 2);
					
					memory.pushLabel(split[0]);
					currentLine = split[1];
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
					
					//remainder is just operands comma and space ", " separated
					//Split line around 'each' comma-space ", " $rs, $rt
					if (currentLine.contains(", "))
						operands = currentLine.split(", ");
					else
						operands = new String[]{currentLine};
					//if no operands, then the only operand will be the ins
					
					if (!ins.equals("no_ins")) {
						Memory.instructions.add(Instruction.buildInstruction(ins, operands));
						memory.attachLabelsToInstruction(ins_index++);
					}
				} else if (parseMode==ParseMode.DATA && currentLine.contains(".")) {
					memory.addData(currentLine.split(" ", 2));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		setup();
		System.out.println("Setup Finished\n");
		boolean hasNextIns = true;
		Instruction ins;
		while (hasNextIns) {
			ins = memory.InstructionFetch();
			ins.execute();
			hasNextIns = !ins.isEXIT();
		}
	}
	
	private enum ParseMode{DATA, TEXT}
}