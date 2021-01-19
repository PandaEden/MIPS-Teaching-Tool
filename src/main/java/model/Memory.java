package model;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/** <pre>
 *  <b>int (signed 32bit)</b>
 *  <i>supports values 2,147,483,647 to - 2,147,483,648</i>
 *  <i>Hex: 0x7FFF,FFFF to -0x800,0000</i>
 *
 *  <b>long (signed 64bit)</b>
 *  <i>supports values 9,223,372,036,854,775,807 to -9,223,372,036,854,775,808</i>
 *  <i>Hex:0x7FFF,FFFF,FFFF,FFFF to -0x800,0000,0000,0000</i>
 *
 *  Code range (.text) 0x00400000 to 0x004FFFFF (4194304 to 5242879)
 *  Global Data (.extern) 0x10010000 to 0x1003FFFF (268500992 to 268697599)
 *  Heap data (.data) 0x10040000 to 0x1FFFFFFF (268697600 to 536870911)
 *  Stack data 0x70000000 to 0x7FFFFFFF (1879048192 to 2147483647)
 * </pre>
 */
public class Memory {
	private static final Memory INSTANCE = new Memory();
	private static final long INS_ADDR_BASE = 0x00400000; //0x 0400 0000
	private static final long DATA_ADDR_BASE = 0x10010000; //0x 1001 0000
	private static long addr_ptr; //decimal representation of address
	private static ArrayList<Long> dataArr= new ArrayList(); //index=(offset from BASE)/4
	private static HashMap<String,Long> labelMap = new HashMap<>();
	private static LinkedList<String> labels = new LinkedList<>();
	
	private Memory( ){ addr_ptr = DATA_ADDR_BASE; } //
	
	/** Given valid input it will add the information to the {@link #dataArr},
	 * It will automatically collect any pushed {@link #labels} and attach to the
	 * address of the first (if a range or array) value added, in {@link #labelMap}
	 *
	 * @param str_args switch:<ul>
	 *<li>.word [±int_Val]:[+int_N]</li>
	 *<li>.word [±int], [±int]... <i>(whitespace ignored)</i></li>
	 *<li>.word [±int]</li>
	 *  </ul>
	 * @return boolean - success of adding all data.
	 * @throws IllegalArgumentException if unsupported input
	 */
	public static boolean addData( @org.jetbrains.annotations.NotNull String[] str_args){
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
	
	/** Given a string it will parse it into the values <b>int_Val</b> representing a signed or
	 * unsigned 32bit integer. and <b>int_N</b> representing the number of values.
	 *
	 * It then creates an array of N integers. and sets all the values to Val.
	 * Stores this array into {@link #dataArr} and increments the {@link #addr_ptr} by 4*N.
	 * <b><pre>String format: "[±int_Val]:[+int_N]"</pre></b>
	 * @param input String to parse.
	 * @return Success of adding all the values.
	 * @see #storeCsvArray(String)
	 * @see #storeArray(long[])
	 * @see #storeWord(long)
	 */
	private static boolean storeRange(String input){
		String[] arr= input.split(":");
		long[] output = new long[Integer.parseInt(arr[1])];//length of range is limited to 2^31
		
		Arrays.fill(output, Long.parseLong(arr[0]));
		return storeArray(output);
	}
	
	/** input String needs to be in the format of a comma separated list of 32bit integers.
	 * whitespace is ignored.
	 * <pre></pre>
	 * Creates an array from the cvs list and stores it into the {@link #dataArr}
	 * and increments the {@link #addr_ptr} by 4*size.
	 * <b><pre>String format: "[±int], [±int]..." <i>(whitespace ignored)</i></pre></b>
	 
	 * @param csvIntArray String of comma separated values to parse.
	 * @return Success of adding all the values.
	 * @see #storeRange(String)
	 * @see #storeArray(long[])
	 * @see #storeWord(long)
	 */
	private static boolean storeCsvArray( String csvIntArray){
		String[] str_arr = csvIntArray.split(",");
		long[] output = new long[str_arr.length];
		
		for(int i=0;i<str_arr.length;i++){
			output[i]=Long.parseLong(str_arr[i].strip());
		}
		return storeArray(output);
	}
	
	/** Given an array for 32bit integers, for each value it adds the value to {@link #dataArr}
	 * , and increments the {@link #addr_ptr} by 4*size.
	 *
	 * @param input Array of 32bit integers (signed or unsigned)
	 * @return Success of adding all the values.
	 * @see #storeRange(String)
	 * @see #storeCsvArray(String)
	 * @see #storeWord(long)
	 */
	private static boolean storeArray(long[] input){
		int falseCounter =0;
		for(Long i: input)
			if (!storeWord(i))
				falseCounter++;
		return !(falseCounter>0);
	}
	
	/** Stores given 32 bit integer (signed or unsigned) into {@link #dataArr} and increments
	 * {@link #addr_ptr} by 4
	 * @param word - signed or unsigned 32bit integer to add
	 * @return Success of adding data.
	 * @see #storeRange(String)
	 * @see #storeCsvArray(String)
	 * @see #storeArray(long[])
	 */
	private static boolean storeWord(long word){
		//TODO: long is used to simplify supporting 32bit Unsigned integers. But this also makes
		// it support non 32-bit integer range, such as (2^32+1 to 2^63-1) and (-2^31-1 to -2^63).
		addr_ptr+=4;
		return dataArr.add(word);
	}
	
	/** Given a decimal address, converts the address to hexadecimal, and contaminates "0x"
	 * datatype indicator before value.
	 * To convert output to just the hex value, use {@link String#substring(int)} with the
	 * parameter '2'.
	 * @param address - decimal address.
	 * @return hexadecimal address in form 0x+hexValue.
	 * @see #toDec(String)
	 */
	public static String toHexAddr( long address){
		return "0x"+Long.toHexString(address);
	}
	
	/** Given a hexadecimal String, it converts the String into a decimal value.
	 * "0x" sign before value is optional.
	 * @param hexString - hexadecimal String to convert to decimal
	 * @return decimal decoding of hexadecimal address
	 * @see #toHexAddr(long)
	 */
	public static long toDec(String hexString){
		return hexString.contains("0x")?
		       Long.decode(hexString) : Long.parseLong(hexString,16);
	}
	
	/** Given a valid decimal address, returns the index in {@link #dataArr} that data is located.
	 * Use {@link #toDec(String)} to convert a hex address to decimal form.
	 * @see Memory#dataArr
	 * @see #toDec(String)
	 * @param decAddress - address of data in Memory, in decimal form.
	 *                   valid 0x00400000 to 0x7FFFFFFF inclusive.
	 * @return the index in {@link #dataArr} the data is located at.
	 */
	public static long getIndex( Long decAddress){
		long index=decAddress - DATA_ADDR_BASE;
		System.out.println( "getting data from data address 0x:"
		                    +toHexAddr(decAddress).substring(7));
		if (index%4!=0)
			throw new IllegalArgumentException( "decAddress:"+ decAddress
			                                    +" needs to be a multiple of 4" );
		
		return dataArr.get(Math.toIntExact(index / 4));
	}
	
	/** Pushes given string to list {@link #labels}
	 * the list is later read, attached to an addresses and cleared by the method
	 * {@link #attachLabelsToAddress(long)}
	 * @see #attachLabelsToAddress(long)
	 * @param label - String to be pushed
	 */
	public static void pushLabel( String label ){
		labels.push(label);
	}
	
	/** given an address (decimal index of address) it will collect all pushed
	 * labels in {@link Memory#labels} and refer them to
	 * @see Memory#toDec(String) to convert hex addresses to decimal
	 * @param addr - address to attach labels to
	 * @throws IndexOutOfBoundsException if addr does not refer to an instruction or data address
	 */
	private static void attachLabelsToAddress( long addr){
		if (addr<INS_ADDR_BASE || addr>0x80000000L)
			throw new IndexOutOfBoundsException ( "Address: "+addr );
		
		for(String label:labels){
			labelMap.put(label, addr);
		}
		labels=new LinkedList<>(); // clear the list of labels once allocated
	}
	
	public static void testPrintAdd(){
		long addr = 0;
		for(String K:labelMap.keySet()
		    ){
			if (K.equals(K.toLowerCase( ))) {
				addr=labelMap.get(K);
				System.out.println("K: "+K+" : V: "+addr);
				System.out.println(Memory.getIndex(addr+4));
			}
		}
		System.out.println(dataArr);
	}
}
