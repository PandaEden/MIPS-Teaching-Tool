import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.ArrayList;
import java.util.Scanner; // Import the Scanner class to read text files

public class Main {
	private static final String InputFile = "FileInput.s";
	
	
	private static ArrayList<String> lines = new ArrayList<>();
	private static ArrayList<String> OpCodes = new ArrayList<>();
	public static void main( String[] args ){
		// Setup, Parse file to lines
		try{ // Try to read input file
			File input = new File( InputFile );
			Scanner reader = new Scanner(input);
			String currentLine = "";
			while(reader.hasNextLine()){
				currentLine = reader.nextLine();
				
				//Remove all whitespace
				currentLine = currentLine.replaceAll("\\s+","");
				//*Remove Comments
				currentLine = currentLine.replaceAll("#.*","");
				if (currentLine.length()>1) { //Skip empty lines
					lines.add(currentLine);
					
					String[] temp = currentLine.split("\\w*(?=\\$)",2);
					//temp
					for(String s : temp){
						System.out.println('\t'+s);
					}
					
				}
			}
		} catch(FileNotFoundException e){
			e.printStackTrace( );
		}
		
		// Parse Lines
		for(String l: lines){
			System.out.println(l);
		}
	}
}