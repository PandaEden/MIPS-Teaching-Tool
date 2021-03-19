package util;

import org.jetbrains.annotations.NotNull;

import model.components.DataMemory;
import model.components.InstrMemory;

import util.validation.Validate;

/**
 Provides common conversions between data types. Input should be checked using appropriate {@link Validate} method
 beforehand to avoid exceptions.
 
 <li>Int - 31bit integer// Immediate</li>
 <li>HEX -  "0x" sign followed by 8 digit Hexadecimal String (Capitalized)</li>
 <li>ADDRESS - unsigned 32bit decimal representing an address</li>
 <li>INDEX - index position for {@link InstrMemory} or {@link model.components.DataMemory}
 or {@link model.components.RegisterBank}</li>
 <li>R_Register - "R0 -R31, or F0-F31"</li>
 <li>Named_Register - "Zero, S0-8, T0-9"</li>
 */
public class Convert {
	
	// Named registers are necessary - but AT,K#,GP,SP,FP are not implemented properly
	public static String[] namedRegisters=splitCSV(
			("ZERO, AT, V0,V1, A0,A1,A2,A3," + "T0,T1,T2,T3,T4,T5,T6,T7,"
			 + "S0,S1,S2,S3,S4,S5,S6,S7," + "T8,T9," + "K1,K2," + "GP,SP,S8, RA").toLowerCase( )
	);//TODO add FP reference.
	
	/**
	 Converts a decimal integer to Hexadecimal String, with "0x" sign prefix.
	 (Negative numbers can be converted back using {@link Integer#toUnsignedLong(int)})
	 
	 @param immediate signed 32bit decimal number to convert.
	 
	 @return (Capitalized), SignExtended 32bit Hex String
	 */
	@NotNull
	public static String int2Hex (@NotNull Integer immediate) {
		StringBuilder rtn=new StringBuilder( Integer.toHexString( immediate ).toUpperCase( ) );
		while ( rtn.length( )<8 ) {
			rtn.insert( 0, "0" );
		}
		return "0x" + rtn;
	}
	
	/**
	 Converts a Hexadecimal String to a signed 32 bit integer.
	 
	 @param hex requires first two chars to be "0x" notation.
	 
	 @return signed 32bit integer value of hex.
	 
	 @throws IllegalArgumentException if hex is invalid format.
	 */
	public static int hex2uInt(@NotNull String hex)  throws IllegalArgumentException{
		if ( hex.startsWith( "0x" ) )
			return Integer.parseUnsignedInt( hex.substring( 2 ), 16 );
		else
			throw new IllegalArgumentException("Does not match Hex format (expects \"0x\" sign)!");
	}
	
	
	/**
	 Converts an address to an immediate ((unsigned)right shift 2 bits aka divide by 4, ignoring remainder)
	 
	 @return address (unsigned)right shifted by 2.
	 
	 @throws IllegalArgumentException not valid
	 */
	@NotNull
	public static Integer address2Imm(@NotNull Integer address) {
		if ( address<0 )
			throw new IllegalArgumentException( "Address are unsigned!" );
		return address >>> 2;
	}
	
	/**
	 Converts an integer into an address (left shift 2 bits aka multiply by 4)
	 <p>
	 <b>Add the returned value to the relevant base address!</b>
	 <p><b>Immediate can be a maximum of (2^26-1) and a minimum of (-2^15)</b>
	 
	 @throws IllegalArgumentException not valid Immediate.
	 @see InstrMemory#BASE_INSTR_ADDRESS
	 @see DataMemory#BASE_DATA_ADDRESS
	 */
	@NotNull
	public static Integer imm2Address(@NotNull Integer immediate) throws IllegalArgumentException{
		final int MIN_IMM=-32768; // (-2^15)
		final int MAX_IMM=(Integer.MAX_VALUE/4)-1; // (536870911)	// Needs to Include Data Memory Space
		
		if ( immediate<MIN_IMM || immediate>MAX_IMM )
			throw new IllegalArgumentException( "Immediate Value \""+immediate+"\" is invalid" );
		return immediate<<2;
	}
	
	/**
	 Converts address to an index position in {@link InstrMemory} or {@link model.components.DataMemory},
	 First removing the base address, then  ((unsigned)right shift 2 bits aka divide by 4, ignoring remainder).
	 <p>
	 //TODO  DataMemory index's are multiples of 2 in this build, to support double-floats.
	 
	 @return index position for {@link InstrMemory} or {@link model.components.DataMemory}
	 
	 @throws IllegalArgumentException for non-valid address
	 @see InstrMemory#BASE_INSTR_ADDRESS
	 @see DataMemory#BASE_DATA_ADDRESS
	 @see InstrMemory#OVER_SUPPORTED_INSTR_ADDRESS
	 @see DataMemory#OVER_SUPPORTED_DATA_ADDRESS
	 */
	@NotNull
	private static Integer address2Index(@NotNull Integer address, String type, int base, int over, String size_name, int size)  throws IllegalArgumentException{
		if ( address<base )
			throw new IllegalArgumentException( "Address Below " + type + " Address!" );	// <- COVER
		else if ( address>=over )
			throw new IllegalArgumentException( "Address OVER_SUPPORTED " + type + " ADDRESS!" );	// <- COVER
		else if ( address%size!=0 )
			throw new IllegalArgumentException( "Data Address is not " + size_name + " aligned!" );	// <- COVER
		else // Valid Data Address
			return (address2Imm( address - base )); // multiple of 2.
	}
	
	public static Integer instrAddr2Index(@NotNull Integer address){
		return address2Index(  address, "Instr", InstrMemory.BASE_INSTR_ADDRESS,
							   InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS, "Word", 4);
	}
	public static Integer dataAddr2Index(@NotNull Integer address){
		return address2Index(  address, "Data", DataMemory.BASE_DATA_ADDRESS,
							   DataMemory.OVER_SUPPORTED_DATA_ADDRESS, "DoubleWord", 8);
	}
	
	/**
	 Converts Comma Separated Values, into an array of the individual values. And additionally strips whitespace.
	 
	 @param CSV String of Comma Separated Values.
	 
	 @return array of individual Strings.
	 */
	@NotNull
	public static String[] splitCSV(@NotNull String CSV) {
		return Convert.removeExtraWhitespace( CSV ).split( "\\s*,\\s*" );
	}
	
	/**
	 Returns the String any whitespace is shortened to only 1 space, and leading/trailing spaces removed.
	 */
	@NotNull
	public static String removeExtraWhitespace(@NotNull String string) {
		// Replace multiple spaces with single space
		string=string.strip( ).replaceAll( "[ \\t]+", " " );
		return string;
	}
	
	/**
	 Converts R style register names "R0-R31" to Named "Zero, S0-8, T0-9".
	 <p>	Expects $ to be removed.
	 
	 @throws IllegalArgumentException Not valid R style Register
	 */
	@NotNull
	public static String r2Named(@NotNull String r_Register) {
		if ( !r_Register.matches( "^r\\d{1,2}$" ) )
			throw new IllegalArgumentException( "Not valid R Register" );	// <- COVER
		
		try {
			int index=Integer.parseInt( r_Register.substring( 1 ) );
			return Convert.namedRegisters[ index ];
		} catch ( ArrayIndexOutOfBoundsException e ) {	// <- COVER
			throw new IllegalArgumentException( "Array Index Out Of Bounds: " + e.getMessage( ) );
		}
	}
	
	/**
	 Converts R style register names "R0-R31" to Index in {@link model.components.RegisterBank}
	 <p>	Expects $ to be removed.
	 
	 @throws IllegalArgumentException Not valid R style Register
	 @see model.components.RegisterBank
	 */
	@NotNull
	public static Integer r2Index(@NotNull String r_Register) {
		if ( !r_Register.matches( "^r\\d{1,2}$" ) )
			throw new IllegalArgumentException( "Not valid R Register" );
		
		int index=Integer.parseInt( r_Register.substring( 1 ) );
		
		if ( index>=namedRegisters.length ) // Tests if the Register is valid index
			throw new IllegalArgumentException( "Register Index Out Of Bounds" );	// <- COVER
		
		return index;
	}
	
	/**
	 Converts Named registers "Zero, S0-8, T0-9" to R style register names "R0-R31".
	 <p>	Expects $ to be removed.
	 
	 @throws IllegalArgumentException Not valid Register Name
	 */
	@NotNull
	public static String named2R(@NotNull String named) throws IllegalArgumentException{
		for ( int i=0; i<Convert.namedRegisters.length; i++ ) {
			if ( Convert.namedRegisters[ i ].equals( named ) ) {
				return "r" + i;
			}
		}
		throw new IllegalArgumentException( "Not Valid Named Register" );
	}
	
	/**
	 Converts Index in {@link model.components.RegisterBank} to R style register names "R0-R31".
	 
	 @throws IllegalArgumentException Not valid Index for register
	 */
	@NotNull
	public static String index2R(@NotNull Integer index) {
		if ( index>=Convert.namedRegisters.length || index<0 ) // Tests if the Register is valid index
			throw new IllegalArgumentException( "Register Index Out Of Bounds" );	// <- COVER
		
		return "r" + index;
	}
	
}
