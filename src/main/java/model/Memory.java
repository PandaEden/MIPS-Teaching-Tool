package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Memory {
	private static final Memory INSTANCE = new Memory();
	private static final String DATA_ADDR_BASE = "10010000"; //0x 1001 0000
	private static final String INS_ADDR_BASE = "0400000"; //0x 0400 0000
	private static long addr_ptr;
	private static ArrayList<Long> data = new ArrayList();
	private static HashMap<String,String> labelMap = new HashMap<>();
	private static String label = "";
	
	private Memory( ){
		addr_ptr = toDec(DATA_ADDR_BASE);
	}
	
	public static boolean addData(String[] str_args){
		labelMap.put(label, toHexAddr(addr_ptr));
		switch (str_args[0]){
			case ".word":
				if (str_args[1].matches("-?\\d*:\\d*"))
					return storeRange(str_args[1]);
				else if (str_args[1].contains(","))
					return storeArray(parseStringToLongArray(str_args[1]));
				else if (str_args[1].matches("-?\\d"))
					return storeWord(Integer.parseInt(str_args[1]));
				break;
			case ".ascii":  // needs to be Null ("\0") Terminated
			case ".asciiz":
				// TODO: String data
			default:
				throw new IllegalStateException("Unexpected value: "+str_args[0]);
		}
		return false;
	}
	
	
	private static boolean storeRange(String input){
		String[] arr= input.split(":");
		long[] output = new long[Integer.parseInt(arr[1])];//length of range is limited to 2^31
		
		Arrays.fill(output, Long.parseLong(arr[0]));
		return storeArray(output);
	}
	
	private static long[] parseStringToLongArray( String csvInt){
		String[] str_arr = csvInt.split(",");
		long[] output = new long[str_arr.length];
		
		for(int i=0;i<str_arr.length;i++){
			output[i]=Long.parseLong(str_arr[i]);
		}
		return output;
	}
	
	private static boolean storeArray(long[] input){
		int falseCount =0;
		for(Long i: input)
			if (!storeWord(i))
				falseCount++;
		return !(falseCount>0);
	}
	
	private static boolean storeWord(long parseLong ){
		addr_ptr+=4;
		return data.add(parseLong);
	}
	
	public static String toHex(long doubleWord){
		return Long.toHexString(doubleWord);
	}
	public static String toHexAddr( long doubleWord){
		String hex = toHex(doubleWord);
		String address = DATA_ADDR_BASE;
		address = address.substring(0,(address.length()-hex.length())).concat(hex);
		return "0x".concat(address);
	}
	public static long toDec(String hex){
		return Long.parseLong(hex,16);
	}
	
	public static long getAddr_ptr( ){
		return addr_ptr;
	}
	
	public static long getIndex( String hexAddress){
		if (hexAddress.length()==10)
			hexAddress = hexAddress.substring(2,hexAddress.length());
		
		long intAddress = toDec(hexAddress);
		intAddress-=toDec(DATA_ADDR_BASE);
		System.out.println( "geting data from data address xxx:"+intAddress );
		return data.get(Math.toIntExact(intAddress / 4));
	}
	
	public static void setLabel( String label ){
		Memory.label=label;
	}
}
