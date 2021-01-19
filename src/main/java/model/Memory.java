package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class Memory {
	private static final Memory INSTANCE = new Memory();
	private static final String DATA_ADDR_BASE = "10010000"; //0x 1001 0000
	private static final String INS_ADDR_BASE = "0400000"; //0x 0400 0000
	private static long addr_ptr; //decimal representation of address
	private static ArrayList<Long> data = new ArrayList(); //index=(offset from BASE)/4
	private static HashMap<String,String> labelMap = new HashMap<>();
	private static LinkedList<String> labels = new LinkedList<>();
	
	private Memory( ){
		addr_ptr = toDec(DATA_ADDR_BASE);
	}
	
	public static boolean addData(String[] str_args){
		attachLabelsToAddress(addr_ptr);
		switch (str_args[0]){
			case ".word":
				if (str_args[1].matches("-?\\d*:\\d*"))
					return storeRange(str_args[1]);
				else if (str_args[1].contains(","))
					return storeCsvArray(str_args[1]);
				else if (str_args[1].matches("-?\\d*"))
					return storeWord(Integer.parseInt(str_args[1]));
				throw new RuntimeException( "no Match found for: "+str_args[1] );
			case ".ascii":  // needs to be Null ("\0") Terminated
			case ".asciiz":
				// TODO: String data
			default:
				throw new IllegalStateException("Unexpected value: "+str_args[0]);
		}
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
	
	private static boolean storeWord(long parseLong ){
		addr_ptr+=4;
		return data.add(parseLong);
	}
	
	public static String toHexAddr( long doubleWord){
		String hex = Long.toHexString(doubleWord);
		//String address = DATA_ADDR_BASE;
		//address = address.substring(0,(address.length()-hex.length())).concat(hex);
		return hex;
	}
	public static long toDec(String hex){
		return Long.parseLong(hex,16);
	}
	
	public static long getIndex( String hexAddress){
		if (hexAddress.length()==10)
			hexAddress = hexAddress.substring(2);
		
		long index=(toDec(hexAddress) - toDec(DATA_ADDR_BASE));
		System.out.println( "getting data from data address 0x:"
		                    +hexAddress.substring(5));
		if (index%4!=0) {
			throw new IllegalArgumentException( "hexAddress:"+ hexAddress
			                                    +" needs to be a multiple of 4" );
		}
		return data.get(Math.toIntExact(index / 4));
	}
	
	public static void pushLabel( String label ){
		labels.push(label);
	}
	private static void attachLabelsToAddress( long addr_ptr){
		for(String label:labels){
			labelMap.put(label, toHexAddr(addr_ptr));
		}
		
		labels=new LinkedList<>(); // clear the list of labels once allocated
	}
}
