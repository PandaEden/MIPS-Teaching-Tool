package model.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.AddressValidation;
import util.Convert;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;

import java.util.ArrayList;

/**
 Wrapper for float[] data.
 <p>
 Index's are double-word aligned (multiple of 8).
 <p>
 Describes actions being performed in the DataLog
 <p>
 use {@link <b><i>AddressValidation#isValidDataAddr(int, ErrorLog)</i></b>} and
 {@link AddressValidation#addr2index(int, ErrorLog)}
 
 @see AddressValidation#addr2index(int, ErrorLog)
 @see #BASE_DATA_ADDRESS
 @see #OVER_SUPPORTED_DATA_ADDRESS */
public class DataMemory{
	public static final int DATA_ALIGN = 8;
	public static final int MAX_DATA_ITEMS = 256;
	public static final int BASE_DATA_ADDRESS = 0x10010000;
	public static final int OVER_SUPPORTED_DATA_ADDRESS = BASE_DATA_ADDRESS+MAX_DATA_ITEMS*DATA_ALIGN;
	public static final int OVER_DATA_ADDRESS = 0x10040000;
	private final String NAME = "DataMemory";
	private final ArrayList<Double> data;
	private final ExecutionLog executionLog;
	
	public DataMemory(@NotNull ArrayList<Double> data, @NotNull ExecutionLog executionLog){
		if (data.size()>MAX_DATA_ITEMS)
			throw new IllegalArgumentException("Data Memory cannot have move than "+MAX_DATA_ITEMS+" indexes");
		
		this.data = data;
		this.executionLog = executionLog;
	}
	
	/**
	 Given a valid Data Address, Returns the Data object for that address.
	 <p>
	 if data has not been set or null input, returns 0 as default value
	 
	 @throws IndexOutOfBoundsException for non-supported address.
	 @see AddressValidation#isSupportedDataAddr(int, ErrorLog)
	 @see #BASE_DATA_ADDRESS
	 @see #OVER_SUPPORTED_DATA_ADDRESS
	 */
	public int readData(@Nullable Integer address){
		int val = 0;
		if (address==null) {
			this.executionLog.append(NAME+":\t"+"No Action!");
		} else if (inRange(address)) {
			int index = toIndex(address);
			if (index<this.data.size())
				val = this.data.get(index).intValue();
			
			this.executionLog.append(NAME+":\t"+"Reading Value["+val+"]\tFrom Memory Address["
					+fmtMem(address, false)+"]!");
		}
		return val;
	}
	
	/**
	 Given a valid Data Address, Sets the Data object for that address.
	 <p> <b>Use {@link AddressValidation#isSupportedDataAddr(int, ErrorLog)} to check input</b>
	 <p>
	 If either input is <i>null</i>, No action is performed.
	 
	 @throws IndexOutOfBoundsException for non-supported address.
	 @see AddressValidation#isSupportedDataAddr(int, ErrorLog)
	 @see #BASE_DATA_ADDRESS
	 @see #OVER_SUPPORTED_DATA_ADDRESS
	 */
	public boolean writeData(@Nullable Integer address, @Nullable Integer data){
		if (address==null || data==null) {
			this.executionLog.append(NAME+":\t"+"No Action!");
			return false;
		} else if (inRange(address)) {
			this.data.add(toIndex(address), data.doubleValue());
			this.executionLog.append(NAME+":\t"+"Writing Value["+data+"]\tTo Memory Address["+fmtMem(address, true)+"]!");
			
		}
		return true;
	}
	
	private int toIndex(int address){
		if (address%DATA_ALIGN!=0 && inRange(address))
			throw new IllegalArgumentException("Address must be Double Word Aligned");
		
		return Convert.address2Index(address)/2;
	}
	
	private boolean inRange(int address){
		if (address>=BASE_DATA_ADDRESS && address<=(OVER_SUPPORTED_DATA_ADDRESS-DATA_ALIGN))
			return true;
		else
			throw new IndexOutOfBoundsException("Data Address must be >="+Convert.uInt2Hex(BASE_DATA_ADDRESS)
					+" and <="+Convert.uInt2Hex(OVER_SUPPORTED_DATA_ADDRESS-DATA_ALIGN)+"!");
	}
	
	private String fmtMem(int address, boolean write){
		final String READ_COL = Logger.Color.PURPLE_ANSI;
		final String WRITE_COL = Logger.Color.BLUE_ANSI;
		
		if (write)
			return Logger.Color.formatColored(WRITE_COL, Convert.uInt2Hex(address));
		else
			return Logger.Color.formatColored(READ_COL, Convert.uInt2Hex(address));
	}

//	/**
//	 Replaces the model {@link DataMemory} encapsulates
//	 */
//	public boolean loadModel(@NotNull ArrayList<Float> data){
//		this.data=data;
//		return true;
//	}
//
//	/**
//	 Returns the data model {@link DataMemory} encapsulates.
//	 */
//	public ArrayList<Double> getModel(){
//		return null;
//	}
}
