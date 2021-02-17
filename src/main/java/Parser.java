

import Util.Log;
import Util.Validate;
import model.components.DataMemory;
import model.instr.InstrMemory;

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
	
	/**
	 Initializes the Parser with an empty model. Either Load a file {@link #loadFile(String)}, and parse it {@link
	#parseFile(String)} Which will also automatically assemble the models ready for execution.
	 <p>
	 Or, parse an array/ single line manually {@link #parseLines(String[])}/{@link #parseLine(String)}(for testing
	 purposes)
	 <p>
	 After you have parsed content. And there are no errors, run {@link #assemble()}
	 
	 @see #assemble()
	 @see #parseFile(String)
	 @see #parseLines(String[])
	 @see #parseLine(String)
	 */
	public Parser(InstrMemory instrMemory, DataMemory dataMemory, Validate validate, Log log){
	
	}
	
	/**
	 Same as {@link #Parser(InstrMemory, DataMemory)}, But, it will automatically run {@link #parseFile(String)} on the parameter.
	 And then additionally run {@link #assemble()}. So you only need to retrieve the models built.
	 <p>(if parser & assembler find no errors!)</p>
	 
	 @param filename address of file to load & parse.
	 @see #parseFile(String)
	 @see #assemble()
	 @see #Parser(InstrMemory, DataMemory)
	 */
	public Parser(String filename, InstrMemory instrMemory, DataMemory dataMemory){
	
	}
	
	/**
	 Apples file checks on the address given.
	 <li>File Exists</li>
	 <li>File is Accessible</li>
	 <li>File is within the Line# limit {@link Util.Validate#Max_Lines}</li>
	 
	 @param filename address of file to load.
	 @return success of loading file.
	 */
	public boolean loadFile(String filename){
		return false;
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
	public boolean parseFile(String filename){
		return false;
	}
	
	/**
	 Parses an individual line for MIPS Syntax.
	 <p>Adds information to emulator's models</p>
	 
	 @param line MIPS instruction to be parsed.
	 @return success of parsing instruction.
	 @see #assemble()
	 */
	public boolean parseLine(String line){
		return false;
	}
	
	/**
	 Parses an array of lines for MIPS Syntax.
	 <p>Adds information to emulator's models</p>
	 
	 @param arr collection of MIPS instructions to be parsed.
	 @return success of parsing all the lines.
	 @see #assemble()
	 */
	public boolean parseLines(String[] arr){
		return false;
	}
	
	/**
	 Finalises the emulator's model by assembling the Labels into addresses.
	 <p>And updates instructions referencing labels with immediate values.</p>
	 <p>
	 After running this, if it returns true, you are safe to retrieve the models.
	 
	 @return success of assembly.
	 */
	public boolean assemble(){
		return false;
	}
	
	/**
	 Empties the current models built by the parser.
	 
	 @see InstrMemory
	 @see DataMemory
	 @see LabelsCache
	 */
	public void clearModels(){
	
	}
	
	boolean parseLabels(){
		return false;
	}
	
	
	private boolean addData(String directive, String data){
		return false;
	}
	
	/**
	 returns Index in {@link DataMemory} of the first data item.
	 
	 -1 on Error
	 */
	private int addData_Word(int word){
		return -1;
	}
	
	/**
	 returns Index in {@link InstrMemory} of the instruction.
	 
	 -1 on Error
	 */
	private int addInstr(int LineNo ,String opcode, String operands){
		return -1;
	}
	
	private boolean parseLabelCache(){
		return false;
	}
	
	/**
	 Intermediary cache for instructions that utilize labels. Since labels can be utilized before the line which declares
	 them, The use of labels correctly can only be checked, after the entire input has been parsed.
	 
	 @see #assemble()
	 */
	private class LabelsCache{
		/*Labels are found in the Parser in 2 scenarios,
		Attaching to the next instruction/data,  or being used by an instruction.
		
		 Where it is attaching to the next instr/data, it should be pushed to a stack/list.
		 	using the returned index from addData / addInstr and the appropriate
		 And add the label to a labelMap with references the address.
		 
		 Where an instruction uses a Label as an operand, It will instead return a placeholder instruction
		 Which (lineNo, opcode, operands, label)
		 
		 When assemble is run, replace any placeholder instructions, with the actual instr with an address,
		  by referencing labelsMap
		*/
		
		/**
		 Adds the label to the labelsStack if {@link Util.Validate#isValidLabel(String)}
		 */
		private boolean pushLabel(String label){
			return false;
		}
		
		/**
		 given an address, pops all the labels off the labelsStack,
		 and adds to the to labelsMap, with the value of the address given.
		 
		 Does not validate the address is valid! - This is done at the assemble stage.
		 */
		private boolean attachLabelsToAddress(int address){
			return false;
		}
		
		/**
		 resets the labelsStack & labelsMap to empty.
		 */
		public void clear(){
		
		}
	}
}
