import model.MemoryBuilder;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.instr.Operands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Convert;
import util.Validate;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.WarningsLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 Responsible for parsing files containing MIPS instructions and building a model for the emulator to run using.
 <p>
 The Parser has 3 main sections: File Checks: Instruction Format: Label assembly:
 <p>
 View the specification on the GitHub for more information of valid MIPS Syntax instructions.
 <p>
 The Parser also logs mistakes with file syntax in an Error & Warnings log. If any Errors are present, these will need to
 be corrected before the emulator will run. Warnings will not prevent execution, and in some cases (e.g. 'nop') might be
 intended by the user.
 <p>
 The Parser will attempt to collect as many errors as possible before terminating to allow the user to correct multiple
 mistakes before re-parsing.
 */
public class Parser{
	private static final String DEFAULT_FILENAME = "FileInput.s";
	private final ErrorLog errorLog;
	private final WarningsLog warningsLog;
	private final MemoryBuilder mb;
	private final Validate val;
	private String errorFn = " NOT SET !";
	private boolean dataLimit = false, instrLimit = false;
	
	/**
	 Initializes the Parser with an empty model. Either Load a file {@link #loadFile(String)}, and parse it {@link
	#loadParseFile(String)} Which will also automatically assemble the models ready for execution.
	 <p>
	 Or, single line manually {@link #parseLine(String, int)}(for testing purposes)
	 <p>
	 After you have parsed content. And there are no errors, run {@link #assemble()}
	 
	 @see #assemble()
	 @see #loadParseFile(String)
	 @see #parseLine(String, int)
	 */
	public Parser(@NotNull MemoryBuilder memoryBuilder, @NotNull ErrorLog errorLog, @NotNull WarningsLog warningsLog){
		this.mb = memoryBuilder;
		this.errorLog = errorLog;
		this.warningsLog = warningsLog;
		this.val = new Validate(errorLog);
	}
	
	/**
	 Same as {@link #Parser(MemoryBuilder, ErrorLog, WarningsLog)}, But, it will automatically run {@link
	#loadParseFile(String)} on the parameter.
	 
	 Run {@link #assemble()} to retrieve the models built. if Null, then there were errors!
	 <p>(if parser & assembler find no errors!)</p>
	 
	 @param filepath address of file to load & parse.
	 
	 @see #loadParseFile(String)
	 @see #assemble()
	 @see #Parser(MemoryBuilder, ErrorLog, WarningsLog)
	 */
	public Parser(@NotNull String filepath, @NotNull MemoryBuilder memoryBuilder,
				  @NotNull ErrorLog errorLog, @NotNull WarningsLog warningsLog){
		this(memoryBuilder, errorLog, warningsLog);
		loadParseFile(filepath);
	}
	
	/**
	 Automatically runs {@link #loadFile(String)} on parameter.
	 <p>Then, parses the file for correct MIPS Syntax,
	 and builds the emulator's models based on it</p>
	 <p>After, you should run {@link #assemble()}</p>
	 
	 @param filename address of file to load & parse.
	 
	 @return success of parsing file.
	 
	 @see #assemble()
	 */
	public boolean loadParseFile(String filename){
		return parseFile(loadFile(filename));
	}
	
	/**
	 Apples file checks on the address given.
	 <li>File Exists</li>
	 <li>File is Accessible</li>
	 
	 @param filename address of file to load.
	 
	 @return File object, if the File is valid
	 */
	public File loadFile(@NotNull String filename){
		if (filename.isBlank()) {
			warningsLog.append("Filename Not Provided, Using Default File: \""+DEFAULT_FILENAME+"\"");
			return null;
		}
		
		errorFn = "File: \""+filename+"\", ";
		File rtn = null;
		try {
			File temp = new File(filename);
			
			String name = temp.getCanonicalPath();
			File parent = temp.getParentFile();
			
			if (parent!=null)
				name = name.substring(parent.getCanonicalPath().length()+1);
			
			errorFn = "File: \""+name+"\", ";
			
			if (!temp.exists()) this.errorLog.append(errorFn+"Does Not Exist!");
			else if (!temp.isFile()) this.errorLog.append(errorFn+"Is Not a File!");
			else if (!temp.canRead()) this.errorLog.append(errorFn+"Can Not Be Read!");
			else rtn=temp;
		} catch (IOException e) {
			errorLog.append(errorFn+"Not Valid FileName!");
			// File is invalid,   rtn is null by default
		}
		return rtn;
	}
	
	/**
	 Parses the file loaded into the parser. Used after running {@link #loadFile(String)}.
	 <p>After, you should run {@link #assemble()}</p>
	 
	 @return success of parsing file.
	 
	 @see #Parser(MemoryBuilder, ErrorLog, WarningsLog)
	 @see #loadFile(String)
	 @see #assemble()
	 */
	public boolean parseFile(File file){
		boolean rtn = true;
		final int MAX_LINES = Validate.MAX_FILE_LINES;
		
		BufferedReader reader = null;
		if (file!=null) {
			try {
				reader = new BufferedReader(new FileReader(file));
				int lineNo = 1;
				
				for(String line = reader.readLine(); line!=null; line=reader.readLine()){
					if (lineNo>=MAX_LINES){
						errorLog.append(errorFn+"Has Too Many Lines!, Max Lines = ["+MAX_LINES+"]!");
						rtn = false;
						break;
					}
					rtn &= parseLine(line, lineNo++);
				}
			} catch (IOException e) {
				errorLog.append(errorFn+"Not Valid FileName!");
			} finally {
				try {
					if (reader!=null) reader.close(); // Close reader
				} catch (IOException e) { e.printStackTrace(); }
			}
		} else
			return false;
		
		return rtn;
	}
	
	/**
	 Parses an individual line for MIPS Syntax.
	 <p>Adds information to emulator's models</p>
	 
	 @param line MIPS instruction to be parsed.
	 
	 @return success of parsing instruction.
	 
	 @see #assemble()
	 */
	public boolean parseLine(@NotNull String line, int lineNo){
		String[] split = splitLine(line);
		int errLength = errorLog.toString().length();
		// parse mode -> ignored
		// validate label
		String label = this.val.isValidLabel(lineNo, split[0]);
		if (label!=null)
			mb.pushLabel(label);
		
		if (errLength<errorLog.toString().length())	// caused by Invalid Label
			errorLog.append("_");
		
		String arg1 = split[1];
		String arg2 = split[2];
		if (arg1!=null && !arg1.isBlank()) {
			// first character is a dot '.'
			if (arg1.matches("\\..*")) {
				// validate directive
				if (this.val.isValidDirective(lineNo, arg1))
					if (!dataLimit && this.val.isDataType(arg1)) // is DataType
						if (!mb.addData(arg1, arg2, errorLog)) { // mb.addData
							if (mb.retrieveData().size()>=DataMemory.MAX_DATA_ITEMS) {
								warningsLog.append("LineNo: "+lineNo+"\tReached MAX Data Size!, No More Data Will Be Parsed!");
								warningsLog.append("\t\t\tData Size Limit == ["+DataMemory.MAX_DATA_ITEMS+"]");
								dataLimit = true;
							}
						}
			} else if (!instrLimit) {
				// validate opcode
				// validate arg2 (operands)
				// mb.addInstr
				if (this.val.isValidOpCode(lineNo, arg1)) {
					Operands operands = this.val.splitValidOperands(lineNo, arg1, arg2, warningsLog);
					if (operands!=null && !mb.addInstruction(arg1, operands)) {
						warningsLog.append("LineNo: "+lineNo+"\tReached MAX Instructions!,"
								+" No More Instructions  Will Be Parsed!");
						warningsLog.append("\t\t\tInstruction Limit == ["+InstrMemory.MAX_INSTR_COUNT+"]");
						instrLimit = true;
					}// TODO Change to report Error
				}
			}
		}
		return (errLength==errorLog.toString().length());
	}
	
	/**
	 Splits a line into it's components
	 <li>[0] - Labels</li>
	 <li>[1] - Directive/ DataType/ Opcode</li>
	 <li>[2] - Data/Operands</li>
	 <li>[3] - Comments</li>
	 */
	@Nullable
	String[] splitLine(@NotNull String line){
		String comment, label, ARG1, ARG2;
		comment = label = ARG1 = ARG2 = null;
		String[] split;
		
		// Split Comments - reserve capitalization
		split = splitComment(Convert.removeExtraWhitespace(line));
		if (split.length==2)
			comment = split[1];
		//
		line = split[0].toLowerCase().strip();    // Make the remainder lowercase
		
		// Split Labels
		if (line.contains(":")) {
			if (line.contains(".")) // if line contains '.'
				split = line.split(":\\s?(?=.*\\.)", 2);    // forward lookup : before .
			else
				split = line.split(":\\s?", 2);
			
			if (split.length==2) {
				label = split[0];
				line = split[1];
			} else
				line = split[0];
		}
		
		if (!line.isBlank()) {    // skip if rest of line is blank
			// Split Arg1 (.directive/ .datatype/ opcode)
			split = line.split("\\s", 2); // split around first white space
			ARG1 = split[0];
			
			if (split.length==2 && !split[1].isEmpty())
				ARG2 = split[1]; // if empty, remain null
		}
		
		return new String[]{label, ARG1, ARG2, comment};
	}
	
	/** Return: [0] contains line, if (length==2) [1] contains comment */
	private String[] splitComment(@NotNull String line){
		// Split at Comment, # or ;
		String[] split;
		
		split = line.split("#", 2);
		String comments = "";
		if (split.length==2)
			comments = "#"+split[1];
		
		line = split[0];
		
		split = line.split(";", 2);
		if (split.length==2)
			comments = ";"+split[1]+comments;
		
		line = split[0];
		
		if (comments.isBlank())
			return new String[]{line};
		else    // contains comments
			return new String[]{line, comments};
	}
	
	/**
	 Wrapper for {@link MemoryBuilder#assembleInstr(ErrorLog)}
	 
	 @return success of assembly.
	 */
	public ArrayList<model.Instruction> assemble(){
		return mb.assembleInstr(errorLog);
	}
	
	public DataMemory getMem(ExecutionLog log){
		return new DataMemory(mb.retrieveData(), log);
	}
}
