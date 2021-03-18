package util.validation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import model.components.DataMemory;
import model.components.InstrMemory;

import util.Convert;
import util.logs.ErrorLog;

/**
 *
 */
public class AddressValidation {
	// TODO [isValidInstrAddr & isValidDataAddr] AND [isSupportedInstrAddr & isSupportedDataAddr]  can be merged
	
	/**
	 Wrapper of {@link Convert#imm2Address(Integer)}
	 <p>
	 First checks it is in range to be converted,
	 if not, returns null and prints to Error Log.
	 */
	@Nullable
	protected static Integer convertValidImm2Addr(int lineNo, @NotNull Integer imm, @NotNull ErrorLog errorLog) {
		try {
			return Convert.imm2Address( imm );
		} catch ( IllegalArgumentException e ) {
			errorLog.append( "LineNo: " + lineNo + "\tImmediate Value: \"" + imm + "\", Cannot Be Converted To A Valid Address!" );
			return null;
		}
	}
	/** Validates the Data address, then converts it to an Index for the {@link DataMemory}
	 <p>
	If not valid, adds to the {@link ErrorLog}, and returns null.
	 
	@see #instrAddr2index(int, ErrorLog) 
	@see ErrorLog
	@see #isSupportedDataAddr(int, ErrorLog)
	@see Convert#dataAddr2Index(Integer)
	 */
	@Nullable
	public static Integer dataAddr2index(int address, ErrorLog errorLog) {
		if ( isSupportedDataAddr( address, errorLog ) )
			return Convert.dataAddr2Index( address );
		return null;
	}
	
	/** Validates the address then converts, it to an Index for the {@link InstrMemory}
	 <p>
	If not valid, adds to the {@link ErrorLog}, and returns null.
	 
	@see #dataAddr2index(int, ErrorLog) 
	 @see ErrorLog
	@see #isSupportedInstrAddr(int, ErrorLog)
	@see Convert#instrAddr2Index(Integer)
	 */
	@Nullable
	public static Integer instrAddr2index(int address,  ErrorLog errorLog) {
			if ( isSupportedInstrAddr( address, errorLog ) )
				return Convert.instrAddr2Index( address );
		return null;
	}
	/**
	 Checks if the address, is a Supported Address for Instructions.
	 <p>And can be used with {@link InstrMemory}, after using {@link #instrAddr2index(int, ErrorLog)} (int, ErrorLog)}.
	 <p>
	 {@link InstrMemory#BASE_INSTR_ADDRESS} <b>>= address <=</b> {@link InstrMemory#OVER_SUPPORTED_INSTR_ADDRESS}
	 -{@link InstrMemory#ADDR_SIZE}.
	 <p>	If not supported, adds to the {@link ErrorLog}.
	 
	 <p> Prints a different error message, if the address is also not valid. using {@link #isValidInstrAddr(int,
			ErrorLog)}
	 
	 @see ErrorLog
	 @see #isValidInstrAddr
	 @see InstrMemory#BASE_INSTR_ADDRESS
	 @see InstrMemory#OVER_SUPPORTED_INSTR_ADDRESS
	 */
	public static boolean isSupportedInstrAddr(int address, ErrorLog errorLog) {
		final int INSTR_SUPPORTED_MAX=InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS - InstrMemory.ADDR_SIZE;
		
		if ( !isValidInstrAddr( address, errorLog ) )
			return false;
		if ( address<=INSTR_SUPPORTED_MAX )
			return true;
		
		errorLog.append( "Instruction Address: \"" + Convert.int2Hex( address ) + "\" Not Supported!" );
		return false;
	}
	
	/**
	 Checks if the address, is a Supported Address for Data.
	 <p>And can be used with {@link DataMemory}, after using {@link #dataAddr2index(int, ErrorLog)} (int, ErrorLog)}.
	 <p>
	 {@link DataMemory#BASE_DATA_ADDRESS} <b>>= address <=</b>  {@link DataMemory#OVER_SUPPORTED_DATA_ADDRESS}
	 -{@link DataMemory#DATA_ALIGN}.
	 <p>	If not supported, adds to the {@link ErrorLog}.
	 
	 <p> Prints a different error message, if the address is also not valid. using {@link #isValidDataAddr(int,
			ErrorLog)}
	 
	 @see ErrorLog
	 @see #isValidDataAddr(int, ErrorLog)
	 @see InstrMemory#BASE_INSTR_ADDRESS
	 @see DataMemory#OVER_SUPPORTED_DATA_ADDRESS
	 */
	public static boolean isSupportedDataAddr(int address, ErrorLog errorLog) {
		final int DATA_SUPPORTED_MAX=DataMemory.OVER_SUPPORTED_DATA_ADDRESS - DataMemory.DATA_ALIGN;
		
		if ( !isValidDataAddr( address, errorLog ) )
			return false;
		if ( address<=DATA_SUPPORTED_MAX )
			return true;
		
		errorLog.append( "Data Address: \"" + Convert.int2Hex( address ) + "\" Not Supported!" );
		return false;
	}
	
	/**
	 {@link InstrMemory#BASE_INSTR_ADDRESS} <b>>= address <=</b> {@link InstrMemory#OVER_INSTR_ADDRESS}
	 -{@link InstrMemory#ADDR_SIZE}.
	 <p>	If not valid, adds to the {@link ErrorLog}.
	 
	 @see ErrorLog
	 @see InstrMemory#BASE_INSTR_ADDRESS
	 @see InstrMemory#OVER_INSTR_ADDRESS
	 */
	static boolean isValidInstrAddr(int address, ErrorLog errorLog) {
		final int INSTR_BASE=InstrMemory.BASE_INSTR_ADDRESS;
		final int SIZE=InstrMemory.ADDR_SIZE;
		final int INSTR_MAX=InstrMemory.OVER_INSTR_ADDRESS - SIZE;
		
		if ( address%SIZE==0 && (address>=INSTR_BASE && address<=INSTR_MAX) )
			return true;
		
		errorLog.append( "Instruction Address: \"" + Convert.int2Hex( address ) + "\" Not Valid!" );
		return false;
	}
	
	/**
	 {@link DataMemory#BASE_DATA_ADDRESS} <b>>= address <=</b> {@link DataMemory#OVER_DATA_ADDRESS}
	 -{@link DataMemory#DATA_ALIGN}.
	 <p>	If not valid, adds to the {@link ErrorLog}.
	 
	 @see ErrorLog
	 @see DataMemory#BASE_DATA_ADDRESS
	 @see DataMemory#OVER_DATA_ADDRESS
	 */
	static boolean isValidDataAddr(int address, ErrorLog errorLog) {
		final int DATA_BASE=DataMemory.BASE_DATA_ADDRESS;
		final int SIZE=DataMemory.DATA_ALIGN;
		final int DATA_MAX=DataMemory.OVER_DATA_ADDRESS - SIZE;
		
		if ( address%SIZE!=0 )
			errorLog.append( "Data Address: \"" + Convert.int2Hex( address ) + "\" Not DoubleWord Aligned!" );
		else if ( address>=DATA_BASE && address<=DATA_MAX )
			return true;
		
		errorLog.append( "Data Address: \"" + Convert.int2Hex( address ) + "\" Not Valid!" );
		return false;
	}
	
}
