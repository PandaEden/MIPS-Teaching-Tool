package model.components;

import Util.Log;
import Util.Validate;

/**
 Wrapper for float[] data.
 
 Index's are word-aligned (multiple of 4).
 
 use {@link Util.Validate#isValidDataAddr(int)} and {@link Util.Validate#addr2index(int)}
 @see Util.Validate#isValidDataAddr(int)
 @see Util.Validate#addr2index(int)
 @see Util.Validate#BASE_DATA_ADDRESS
 @see Util.Validate#MAX_DATA_ADDRESS
 */
public class DataMemory{
	
	/**
	 @see #loadModel(float[])
	 */
	DataMemory(float[] data,Validate validator, Log log){
		//TODO might not actually need access to log, if using Validate
	}
	
	/**
	 Sets address gives to the data value given.
	 If address is invalid, it adds to the ErrorLog.
	 */
	public boolean setData(int address, int data){
		return false;
	}
	
	/**
	 Given a valid Data Address, Returns the Data object for that address.
	 <p>
	 Fetching from address without data:
	 <li>below base address - Error</li>
	 <li>above max address - Error</li>
	 <li>valid address - returns default value 0</li>
	 */
	public int getData(int address){
		return -1;
	}
	
	/**
	 Replaces the model {@link DataMemory} encapsulates
	 @throws NullPointerException
	 */
	public boolean loadModel(float[] data){
		return false;
	}
	
	/**
	 Returns the data model {@link DataMemory} encapsulates.
	 */
	public float[] getModel(){
		return null;
	}
}
