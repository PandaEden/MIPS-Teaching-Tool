package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class Memory {
	private static final long INS_ADDR_BASE = 0x00400000; //0x 0400 0000
	private static final long DATA_ADDR_BASE = 0x10010000; //0x 1001 0000
	private static long addr_ptr = DATA_ADDR_BASE; //decimal representation of address
	private static final ArrayList<Long> dataArr= new ArrayList<>(); //index=(offset from BASE)/4
	private static final HashMap<String,Long> labelMap = new HashMap<>();
	private static LinkedList<String> labels = new LinkedList<>();
	
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
	
	
	private static boolean storeRange(String input){
		String[] arr= input.split(":");
		long[] output = new long[Integer.parseInt(arr[1])];//length of range is limited to 2^31
		
		Arrays.fill(output, Long.parseLong(arr[0]));
		return storeArray(output);
	}
	private static boolean storeCsvArray( String csvIntArray){
		String[] str_arr = csvIntArray.split(",");
		long[] output = new long[str_arr.length];
		
		for(int i=0;i<str_arr.length;i++){
			output[i]=Long.parseLong(str_arr[i].strip());
		}
		return storeArray(output);
	}
	
	private static boolean storeArray(long[] input){
		int falseCounter =0;
		for(Long i: input)
			if (!storeWord(i))
				falseCounter++;
		return !(falseCounter>0);
	}
	
	private static boolean storeWord(long word ){
		addr_ptr+=4;
		return dataArr.add(word);
	}
	
	public static String toHexAddr( long address){
		return "0x"+Long.toHexString(address);
	}
	
	public static long toDec(String hexString){
		return hexString.contains("0x")?
		       Long.decode(hexString) : Long.parseLong(hexString,16);
	}
	
	public static long getIndex( Long decAddress){
		long index=decAddress - DATA_ADDR_BASE;
		System.out.println( "getting data from data address 0x:"
		                    +toHexAddr(decAddress).substring(7));
		if (index%4!=0)
			throw new IllegalArgumentException( "decAddress:"+ decAddress
			                                    +" needs to be a multiple of 4" );
		
		return dataArr.get(Math.toIntExact(index / 4));
	}
	
	public static void pushLabel( String label ){
		labels.push(label);
	}
		private static void attachLabelsToAddress( long addr){
		if (addr<INS_ADDR_BASE || addr>0x80000000L)
			throw new IndexOutOfBoundsException ( "Address: "+addr );
		
		for(String label:labels){
			labelMap.put(label, addr);
		}
		labels=new LinkedList<>(); // clear the list of labels once allocated
	}
}
