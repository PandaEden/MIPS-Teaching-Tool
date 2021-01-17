import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner; // Import the Scanner class to read text files
import model.Instruction;
import model.Memory;

public class Main {
	private static final String InputFile = "FileInput.s";
	private enum ParseMode{DATA,TEXT}
	private static ParseMode parseMode = ParseMode.TEXT;
	private static ArrayList<Instruction> instructions;
	
	public static void setup(){
		instructions=new ArrayList<>();
		// Setup: Parse file to lines
		try{ // Try to read input file
			File input = new File( InputFile );
			Scanner reader = new Scanner(input);
			String currentLine = "";
			
			String ins, label, comments=null;
			String[] operands;
			//Per Line actions - Instruction Parsing
			
			while(reader.hasNextLine()){
				//Get Next Line
				currentLine = reader.nextLine();
				ins = "no_ins";
				String[] split=null;
				
				//Split Line around comment, first "#"
				if (currentLine.contains("#")) {
					split=currentLine.split("#", 2);
					
					currentLine=split[0];
					comments="#"+split[1]; // append after #, so # is included in comment.
				}
				
				//SET_PARSE_MODE
				if (currentLine.toLowerCase().contains(".data")) {
					parseMode=ParseMode.DATA;
					continue;
				}else if (currentLine.toLowerCase().contains(".text")){
					parseMode=ParseMode.TEXT;
					continue;
				}
				//Split line around Tag, first ":"
				if (currentLine.contains(":")) {
					split=currentLine.split(":", 2);
					
					Memory.pushLabel(split[0]);
					currentLine=split[1];
				}
				currentLine=currentLine.trim();//Trim whitespace
				if (parseMode==ParseMode.TEXT) {
					
					//Split line around first space, ins" "$first_operand
					if (currentLine.contains(" ")) {
						split=currentLine.split(" ", 2);
						
						ins=split[0];
						currentLine=split[1];//Remainder should just be operands
					}
					
					//remainder is just operands comma and space ", " separated
					//Split line around 'each' comma-space ", " $rs, $rt
					if (currentLine.contains(", ")) operands=currentLine.split(", ");
					else operands=new String[] { currentLine };
					if (!ins.equals("no_ins")) {
						instructions.add(Instruction.buildInstruction(ins, operands, comments));
					}
				}else if (parseMode==ParseMode.DATA&&currentLine.contains(".")){
					Memory.addData(currentLine.split(" ",2));
				}
			}
		} catch(FileNotFoundException e){
			e.printStackTrace( );
		}
	}
	
	public static void main( String[] args ){
		setup();
		System.out.println( "Setup Finished\n" );
		instructions.forEach(Instruction::execute);
	}
}