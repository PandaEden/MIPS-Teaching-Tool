package Util;

import model.components.InstrMemory;

/**
 Provides common conversions between data types. Does not check if input is valid. Input should be checked using
 appropriate {@link Validate} method beforehand.
 
 <li>DEC - decimal signed 32bit integer</li>
 <li>HEX -  "0x" sign followed by 8 digit Hexadecimal String (Capitalized)</li>
 <li>ADDRESS - unsigned 32bit decimal representing an address</li>
 <li>INDEX - index position for {@link InstrMemory} or {@link model.components.DataMemory}
 or {@link model.components.RegisterBank}</li>
 <li>R_Register - "R0 -R31, or F0-F31"</li>
 <li>Named_Register - "Zero, S0-8, T0-9"</li>
 */
public class Convert{
	
	// Named registers are necessary - but AT,K#,GP,SP,FP are not implemented properly
	public static String[] namedRegisters = splitCSV(
			("ZERO, AT, V0,V1, A0,A1,A2,A3,"+"T0,T1,T2,T3,T4,T5,T6,T7,"
					+"S0,S1,S2,S3,S4,S5,S6,S7"+"T8,T9"+"K1,K2"+"GP,SP,S8, RA").toLowerCase()
	);//TODO add FP reference.
	
	/**
	 Converts a decimal integer to (8 digit)Hexadecimal_(10 char)String
	 
	 @param decimal signed 32bit decimal number to convert.
	 @return "0x########" (Capitalized)
	 */
	static String int2hex(int decimal){
		return null;
	}
	
	/**
	 Converts a Hexadecimal String to a signed 32 bit integer.
	 
	 @param hex (8 digit)Hexadecimal_(10 char)String.
	 @return signed 32bit integer value of hex.
	 @throws IllegalArgumentException if hex is invalid format.
	 */
	static int hex2int(String hex){
		return -1;
	}
	
	/**
	 Converts an address to an index (right shift 2 bits == divide by 4)
	 
	 @return index position for {@link InstrMemory} or {@link model.components.DataMemory}, or -1 if not valid
	 address
	 @see Validate#BASE_INSTR_ADDRESS
	 @see Validate#BASE_DATA_ADDRESS
	 @see Validate#OVER_INSTR_ADDRESS
	 @see Validate#OVER_DATA_ADDRESS
	 */
	public static int address2index(int address){
		return -1;
	}
	
	/**
	 Converts an integer into an address (left shift 2 bits == multiply by 4)
	 <p>
	 <b>Add the returned value to the relevant base address!</b>
	 
	 @see Validate#BASE_INSTR_ADDRESS
	 @see Validate#BASE_DATA_ADDRESS
	 */
	public static int int2address(int integer){
		return -1;
	}
	
	/**
	 Converts Comma Separated Values, into an array of the individual values. And additionally strips whitespace.
	 
	 @param CSV String of Comma Separated Values.
	 @return array of individual Strings.
	 */
	static String[] splitCSV(String CSV){
		return null;
	}
	
	/**
	 Converts R style register names "R0-R31" to Named "Zero, S0-8, T0-9".
	 */
	public static String r2named(String r_Register){
		return null;
	}
	
	/**
	 Converts R style register names "R0-R31" to Index in {@link model.components.RegisterBank}
	 
	 @see model.components.RegisterBank
	 */
	public static int r2index(String r_Register){
		return -1;
	}
	
	/**
	 Converts Named registers "Zero, S0-8, T0-9" to R style register names "R0-R31".
	 */
	public static String named2r(String named_Register){
		return null;
	}
	
	/**
	 Converts Index in {@link model.components.RegisterBank} to R style register names "R0-R31".
	 */
	public static String index2r(int index){
		return null;
	}
}
