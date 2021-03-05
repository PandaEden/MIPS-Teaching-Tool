package model;

import model.components.DataMemory;
import model.components.InstrMemory;
import model.instr.Operands;
import org.jetbrains.annotations.NotNull;
import util.Convert;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 <pre>
 <b>int (signed 32bit)</b>
 <i>supports values 2,147,483,647 to - 2,147,483,648</i>
 <i>Hex: 0x7FFF,FFFF to -0x800,0000</i>
 
 <b>long (signed 64bit)</b>
 <i>supports values 9,223,372,036,854,775,807 to -9,223,372,036,854,775,808</i>
 <i>Hex:0x7FFF,FFFF,FFFF,FFFF to -0x800,0000,0000,0000</i>
 
 Code range (.text) 0x00400000 to 0x004FFFFF (4194304 to 5242879)
 Global Data (.data) 0x10010000 to 0x1003FFFF (268500992 to 268697599)
 Heap data 0x10040000 to 0x1FFFFFFF (268697600 to 536870911)
 Stack data 0x70000000 to 0x7FFFFFFF (1879048192 to 2147483647)
 In powers:
 0x00400000:(2^22)       >= Code     <0x00500000:(2^22 +2^20)
 0x10010000:(2^28 +2^16) >= Global   <0x10040000:(2^28 +2^18)
 0x10040000:(2^28 +2^18) >= Heap     <0x20000000:(2^29)
 0x70000000:(2^31-2^28)  >= Stack    <0x80000000:(2^31)
 </pre>
 */
public class MemoryBuilder{
	private static final int INS_ADDR_BASE = InstrMemory.BASE_INSTR_ADDRESS; //0x 0400 0000
	private static final int DATA_ADDR_BASE = DataMemory.BASE_DATA_ADDRESS; //0x 1001 0000
	private static final int LIMIT = InstrMemory.MAX_INSTR_COUNT;
	private static final int ADDR_SIZE = InstrMemory.ADDR_SIZE;
	private static final int DATA_SIZE = DataMemory.DATA_ALIGN;
	
	private final HashMap<Integer, Double> dataArr = new HashMap<Integer, Double>(); //index=(offset from BASE)/DataMemory.DATA_ALIGN;
	private final DataMemory dataMem = new DataMemory(dataArr, new ExecutionLog(new ArrayList<String>()));
	private final HashMap<String, Integer> labelMap = new HashMap<>();
	private final LinkedList<String> labels = new LinkedList<>();
	
	private ArrayList<Instruction> instructions = new ArrayList<>();
	private int ProgramCounter = INS_ADDR_BASE;
	private int MEM_PTR = DATA_ADDR_BASE; //decimal representation of address
	
	/**
	 Given valid input it will add the information to the {@link #dataArr},
	 It will automatically collect any pushed {@link #labels} and attach to the
	 address of the first (if a range or array) value added, in {@link #labelMap}
	 
	 @param str_args switch:<ul>
	 <li>.word [±int_Val]:[+int_N]</li>
	 <li>.word [±int], [±int]... <i>(whitespace ignored)</i></li>
	 <li>.word [±int]</li>
	 </ul>
	 
	 @return boolean - success of adding all data.
	 
	 @throws IllegalArgumentException if unsupported input
	 */
	public boolean addData(@NotNull String[] str_args){
		final String SIGNED_INT = "-?\\d*";	// matches optional '-' sign, then any length int
		final String POSITIVE_INT = "\\d*";	// matches any length int
		final String OptSpace = "\\s?";		// Optional Single Whitespace
		
		final String CSV_REGEX = "("+SIGNED_INT+OptSpace+","+OptSpace+")"+SIGNED_INT; // (-?\\d*\\s,\\s)-?\\d*
		final String COLON_SEP = SIGNED_INT+OptSpace+":"+OptSpace+POSITIVE_INT; // -?\\d*\\s:\\s\\d*
		
		attachLabelsToAddress(MEM_PTR);
		// Validate.isValidDataType // Directive
		if (".word".equals(str_args[0])) {
			if		(str_args[1].matches(COLON_SEP)) return storeRange(str_args[1]);	// Val:N
			else if (str_args[1].matches(CSV_REGEX)) return storeCsvArray(str_args[1]);	// Int, Int, Int ...
			else if (str_args[1].matches(SIGNED_INT)) return storeWord(Integer.parseInt(str_args[1]));	// INT
			else
				throw new IllegalArgumentException("Data: ["+str_args[1]+"], Not Valid For DataType: "+str_args[0]);
		}
		throw new IllegalArgumentException("Not Supported DataType: "+str_args[0]);
	}
	
	/**
	 Given a string it will parse it into the values <b>int_Val</b> representing a signed 32bit integer.
	 And <b>int_N</b> representing the number of values.
	 <p>
	 It then creates an array of <b>int_N</b> integers, and sets all the values to <b>int_Val</b>.
	 <p>
	 Then, Stores this array into {@link #dataArr} and increments the {@link #MEM_PTR} by {@link #DATA_SIZE}*<b>int_N</b>
	 <b><pre>String format: "[±int_Val]:[+int_N]"</pre></b>
	 
	 @return Success of adding all of the values.
	 
	 @see #storeCsvArray(String)
	 @see #storeArray(int[])
	 @see #storeWord(int)
	 */
	private boolean storeRange(String input){
		String[] arr = input.split("\\s:\\s");
		int[] output = new int[Integer.parseInt(arr[1])];//length of range is limited to 2^31
		
		Arrays.fill(output, Integer.parseInt(arr[0]));
		return storeArray(output);
	}
	
	/**
	 input String needs to be in the format of a comma separated list of 32bit integers.
	 whitespace is ignored.
	 <pre></pre>
	 Creates an Array from the CSV list and stores it into the {@link #dataArr} and increments the {@link #MEM_PTR} by
	 {@link #DATA_SIZE}*size.ofArray.
	 <b><pre>String format: "[±int], [±int]..." <i>(whitespace ignored)</i></pre></b>
	 
	 @param csvIntArray String of comma separated values to parse.
	 
	 @return Success of adding all of the values.
	 
	 @see #storeRange(String)
	 @see #storeArray(int[])
	 @see #storeWord(int)
	 */
	private boolean storeCsvArray(String csvIntArray){
		String[] str_arr = Convert.splitCSV(csvIntArray);
		int[] output = new int[str_arr.length];
		
		for (int i = 0; i<str_arr.length; i++) {
			output[i] = Integer.parseInt(str_arr[i].trim());
		}
		return storeArray(output);
	}
	
	/**
	 Given an array of signed 32bit integers, for each value it adds the value to {@link #dataArr}
	 , and increments the {@link #MEM_PTR} by {@link #DATA_SIZE}*size.ofArray.
	 
	 @return Success of adding all of the values.
	 
	 @see #storeRange(String)
	 @see #storeCsvArray(String)
	 @see #storeWord(int)
	 */
	private boolean storeArray(int[] input){
		for (int i : input)
			if (!storeWord(i))
				return false;
		return true;
	}
	
	/**
	 Stores given signed 32 bit integer into {@link #dataArr} and increments {@link #MEM_PTR} by {@link #DATA_SIZE}
	 
	 @return Success of adding data.
	 
	 @see #storeRange(String)
	 @see #storeCsvArray(String)
	 @see #storeArray(int[])
	 */
	private boolean storeWord(int word){
		//dataArr.add((double) word);
		if (MEM_PTR<DATA_ADDR_BASE+LIMIT*DATA_SIZE){
			dataMem.writeData(MEM_PTR, word);
			MEM_PTR += DATA_SIZE;
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 Pushes given string to list {@link #labels}
	 the list is later read, attached to an addresses and cleared by the method
	 {@link #attachLabelsToAddress(int)}
	 
	 @param label - String to be pushed
	 
	 @see #attachLabelsToAddress(int)
	 */
	public void pushLabel(String label){
		labels.push(label);
	}
	
	/**
	 Given an address (decimal index of address) it will collect all pushed
	 <b>labels</b> in {@link MemoryBuilder#labels} and maps them to the provided
	 <b>addr</b> in {@link MemoryBuilder#labelMap}
	 <pre>use {toDec(String)} to convert hex addresses to decimal form.</pre>
	 Memory#toDec(String)
	 
	 @param addr - address to attach labels to
	 
	 @throws IndexOutOfBoundsException if addr does not refer to an instruction or data address
	 */
	private void attachLabelsToAddress(int addr){
		if (addr<INS_ADDR_BASE || addr>DataMemory.OVER_DATA_ADDRESS)
			throw new IndexOutOfBoundsException("Address: "+addr);
		
		for (String label : labels) {
			labelMap.put(label, addr);
		}
		labels.clear(); // clear the list of labels once allocated
	}
	
	/**
	 Given the index of an instruction (in the instructions list),
	 collect pushed labels in {@link MemoryBuilder#labels} and maps them to the calculated
	 instruction address, in {@link MemoryBuilder#labelMap}.
	 
	 @see MemoryBuilder#attachLabelsToAddress(int)
	 */
	public boolean addInstruction(String opcode, Operands operands){
		if (ProgramCounter<(INS_ADDR_BASE+LIMIT*ADDR_SIZE)) {
			int index = Convert.address2Index(ProgramCounter);
			int address = INS_ADDR_BASE+Convert.imm2Address(index);
			
			instructions.add(index, Instruction.buildInstruction(opcode, operands));
			attachLabelsToAddress(address);
			return true;
		}
		return false;
	}
	
	public HashMap<Integer, Double> retrieveData(){
		return dataArr;
	}
	
	/**
	 @return null means error during assembly, and application should be terminated.
	 */
	public ArrayList<Instruction> assembleInstr(ErrorLog errorLog){
		if (instructions.isEmpty()) {
			errorLog.append("No Instructions Found!");
		} else {
			boolean assembled = true;
			for (Instruction i : instructions) {
				assembled &= i.assemble(errorLog, labelMap);
			}
			if (assembled)
				return instructions;
			else
				errorLog.append("Failed To Assemble Instructions!");
		}
		return null;
	}
}