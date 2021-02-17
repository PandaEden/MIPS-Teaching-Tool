package Util;

/**
 Provides common conversions between data types. Does not check if input is valid. Input should be checked using
 appropriate {@link Validate} method beforehand.
 
 <li>DEC - decimal signed 32bit integer</li>
 <li>HEX -  "0x" sign followed by 8 digit Hexadecimal String (Capitalized)</li>
 <li>ADDRESS - unsigned 32bit decimal representing an address</li>
 <li>INDEX - index position for {@link model.instr.InstrMemory} or {@link model.components.DataMemory}
 or {@link RegisterBank}</li>
 <li>R_Register - "R0 -R31, or F0-F31"</li>
 <li>Named_Register - "Zero, S0-8, T0-9"</li>
 */
public class Convert{
	
	/**
	 Converts a decimal integer to (8 digit)Hexadecimal_(10 char)String
	 
	 @param decimal signed 32bit decimal number to convert.
	 @return "0x########" (Capitalized)
	 */
	private static String dec2hex(int decimal){
		return null;
	}
	
	/**
	 Converts a Hexadecimal String to a signed 32 bit integer.
	 
	 @param hex (8 digit)Hexadecimal_(10 char)String.
	 @return signed 32bit integer value of hex.
	 */
	private static int hex2dec(String hex){
		return -1;
	}
	
	/**
	 Converts an address to an index (right shift 2 bits == divide by 4)
	 
	 @return index position for {@link model.instr.InstrMemory} or {@link model.components.DataMemory}
	 */
	public static int address2index(int address){
		return -1;
	}
	
	/**
	 Converts an index into an address (left shift 2 bits == multiply by 4)
	 
	 Add the returned value to the relevant base address.
	 @see Validate#BASE_INSTR_ADDRESS
	 @see Validate#BASE_DATA_ADDRESS
	 */
	public int index2address(){
		return -1;
	}
	
	/**
	 Converts Comma Separated Values, into an array of the individual values. And additionally strips whitespace.
	 
	 @param CSV String of Comma Separated Values.
	 @return array of individual Strings.
	 */
	private static String[] splitCSV(String CSV){
		return null;
	}
	
	/**
	 Converts R style register names "R0-R31" to Named "Zero, S0-8, T0-9".
	 */
	public static String r2named(String r_Register){
		return null;
	}
	
	/**
	 Converts R style register names "R0-R31" to Index in {@link RegisterBank}
	 
	 @see RegisterBank
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
	 Converts Index in {@link RegisterBank} to R style register names "R0-R31".
	 */
	public static String index2r(int index){
		return null;
	}
}
