package model;

import model.components.DataMemory;
import org.jetbrains.annotations.NotNull;
import util.Convert;
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
public class Memory{
	private static final int INS_ADDR_BASE = 0x00400000; //0x 0400 0000
	private static final int DATA_ADDR_BASE = DataMemory.BASE_DATA_ADDRESS; //0x 1001 0000
	private static final ArrayList<Double> dataArr = new ArrayList<>(); //index=(offset from BASE)/DataMemory.DATA_ALIGN;
	private static final HashMap<String, Long> labelMap = new HashMap<>();
	private static final LinkedList<String> labels = new LinkedList<>();
	public static ArrayList<Instruction> instructions = new ArrayList<>();
	public static long ProgramCounter = INS_ADDR_BASE;
	private static int addr_ptr = DATA_ADDR_BASE; //decimal representation of address
	private static final DataMemory data = new DataMemory(new ArrayList<Double>(),
			new ExecutionLog(new ArrayList<String>()));
	
	public static long getLabelAddress(String label){
		return labelMap.get(label);
	}
	
	//TODO - Placeholder until refacotring complete
	public static Integer getData(int address){
		return data.readData(address);
	}
	
	//TODO - Placeholder until refacotring complete
	public static void putData(int address, int value){
		data.writeData(address, value);
	}
	
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
		attachLabelsToAddress(addr_ptr);
		if (".word".equals(str_args[0])) {
			if (str_args[1].matches("-?\\d*:\\d*")) return storeRange(str_args[1]);
			else if (str_args[1].contains(",")) return storeCsvArray(str_args[1]);
			else if (str_args[1].matches("-?\\d*")) return storeWord(Integer.parseInt(str_args[1]));
			throw new IllegalArgumentException("data type: "+str_args[1]+" not supported");
			/*
			case ".ascii":  // needs to be Null ("\0") Terminated
			case ".asciiz":
			case ".float":
			case ".double":
			*/
		}
		throw new IllegalArgumentException("Unexpected value: "+str_args[0]);
	}
	
	/**
	 Given a string it will parse it into the values <b>int_Val</b> representing a signed or
	 unsigned 32bit integer. and <b>int_N</b> representing the number of values.
	 <p>
	 It then creates an array of N integers. and sets all the values to Val.
	 Stores this array into {@link #dataArr} and increments the {@link #addr_ptr} by 4*N.
	 <b><pre>String format: "[±int_Val]:[+int_N]"</pre></b>
	 
	 @param input String to parse.
	 
	 @return Success of adding all the values.
	 
	 @see #storeCsvArray(String)
	 @see #storeArray(long[])
	 @see #storeWord(long)
	 */
	private boolean storeRange(String input){
		String[] arr = input.split(":");
		long[] output = new long[Integer.parseInt(arr[1])];//length of range is limited to 2^31
		
		Arrays.fill(output, Long.parseLong(arr[0]));
		return storeArray(output);
	}
	
	/**
	 input String needs to be in the format of a comma separated list of 32bit integers.
	 whitespace is ignored.
	 <pre></pre>
	 Creates an array from the cvs list and stores it into the {@link #dataArr}
	 and increments the {@link #addr_ptr} by 4*size.
	 <b><pre>String format: "[±int], [±int]..." <i>(whitespace ignored)</i></pre></b>
	 
	 @param csvIntArray String of comma separated values to parse.
	 
	 @return Success of adding all the values.
	 
	 @see #storeRange(String)
	 @see #storeArray(long[])
	 @see #storeWord(long)
	 */
	private boolean storeCsvArray(String csvIntArray){
		String[] str_arr = csvIntArray.split(",");
		long[] output = new long[str_arr.length];
		
		for (int i = 0; i<str_arr.length; i++) {
			output[i] = Long.parseLong(str_arr[i].trim());
		}
		return storeArray(output);
	}
	
	/**
	 Given an array for 32bit integers, for each value it adds the value to {@link #dataArr}
	 , and increments the {@link #addr_ptr} by 4*size.
	 
	 @param input Array of 32bit integers (signed or unsigned)
	 
	 @return Success of adding all the values.
	 
	 @see #storeRange(String)
	 @see #storeCsvArray(String)
	 @see #storeWord(long)
	 */
	private boolean storeArray(long[] input){
		for (Long i : input)
			if (!storeWord(i))
				return false;
		return true;
	}
	
	/**
	 Stores given 32 bit integer (signed or unsigned) into {@link #dataArr} and increments
	 {@link #addr_ptr} by 4
	 
	 @param word - signed or unsigned 32bit integer to add
	 
	 @return Success of adding data.
	 
	 @see #storeRange(String)
	 @see #storeCsvArray(String)
	 @see #storeArray(long[])
	 */
	private boolean storeWord(long word){
		//TODO: long is used to simplify supporting 32bit Unsigned integers. But this also makes
		// it support non 32-bit integer range, such as (2^32+1 to 2^63-1) and (-2^31-1 to -2^63).
		addr_ptr += DataMemory.DATA_ALIGN;
		return dataArr.add((double) word);
	}
	
	/**
	 Pushes given string to list {@link #labels}
	 the list is later read, attached to an addresses and cleared by the method
	 {@link #attachLabelsToAddress(long)}
	 
	 @param label - String to be pushed
	 
	 @see #attachLabelsToAddress(long)
	 */
	public void pushLabel(String label){
		labels.push(label);
	}
	
	/**
	 Given an address (decimal index of address) it will collect all pushed
	 <b>labels</b> in {@link Memory#labels} and maps them to the provided
	 <b>addr</b> in {@link Memory#labelMap}
	 <pre>use {toDec(String)} to convert hex addresses to decimal form.</pre>
	 Memory#toDec(String)
	 
	 @param addr - address to attach labels to
	 
	 @throws IndexOutOfBoundsException if addr does not refer to an instruction or data address
	 */
	private void attachLabelsToAddress(long addr){
		if (addr<INS_ADDR_BASE || addr>0x80000000L)
			throw new IndexOutOfBoundsException("Address: "+addr);
		
		for (String label : labels) {
			labelMap.put(label, addr);
		}
		labels.clear(); // clear the list of labels once allocated
	}
	
	/**
	 Given the index of an instruction (in the instructions list),
	 collect pushed labels in {@link Memory#labels} and maps them to the calculated
	 instruction address, in {@link Memory#labelMap}.
	 
	 @param index_counter - index of instruction in <b>instructions</b>
	 
	 @see Memory#attachLabelsToAddress(long)
	 */
	public void attachLabelsToInstruction(int index_counter){
		long address = INS_ADDR_BASE+(index_counter*4L);
		attachLabelsToAddress(address);
	}
	
	public Instruction InstructionFetch(){
		long pc = ProgramCounter;
		ProgramCounter += 4;
		return instructions.get(Convert.address2Index((int) pc));
	}
	
}