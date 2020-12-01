import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner; // Import the Scanner class to read text files
import model.Instruction;
public class Main {
	private static final String InputFile = "FileInput.s";
	
	private static ArrayList<Instruction> instructions;
	
	public static void main( String[] args ){
		instructions=new ArrayList<>();
		// Setup: Parse file to lines
		try{ // Try to read input file
			File input = new File( InputFile );
			Scanner reader = new Scanner(input);
			String currentLine = "";
			
			String ins, tag, comments;
			String[] opperands;
			//Per Line actions - Instruction Parsing
			while(reader.hasNextLine()){
				//Get Next Line
				currentLine = reader.nextLine();
				ins = "no_ins";
				tag = new String();
				comments = new String(); // pre append # to comment
				opperands = null;
				String[] split=null;
				
				//Split Line around comment, first "#"
				if (currentLine.contains("#")) {
					split=currentLine.split("#", 2);
					
					currentLine=split[0];
					comments="#"+split[1]; // append after #, so # is included in comment.
				}
				
				//Split line around Tag, first ":"
				if (currentLine.contains(":")) {
					split=currentLine.split(":", 2);
					
					tag=split[0]+":"; // append ":" after tag
					currentLine=split[1];
				}
				//Trim whitespace
				currentLine=currentLine.trim();
				
				//Split line around first space, ins" "$first_opperand
				if (currentLine.contains(" ")){
					split=currentLine.split(" ",2);
					
					ins=split[0];
					currentLine=split[1];//Remainder should just be opperands
				}
				
				//remainder is just opperands comma and space ", " seperated
				//Split line around 'each' comma-space ", " $rs, $rt
				if (currentLine.contains(", "))
					opperands=currentLine.split(", ");
				else
					opperands=new String[]{currentLine};
				
				instructions.add(new Instruction(ins,opperands,comments,tag));
			}
		} catch(FileNotFoundException e){
			e.printStackTrace( );
		}
		instructions.forEach(instruction -> System.out.println( instruction ));
	}
}