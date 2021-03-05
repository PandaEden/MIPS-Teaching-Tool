package util;

import model.components.DataMemory;
import model.components.InstrMemory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.logs.ErrorLog;

/**
 *
 */
public class AddressValidation{
	/**
	 {@link InstrMemory#BASE_INSTR_ADDRESS} <b>>= address <=</b> {@link InstrMemory#OVER_INSTR_ADDRESS}
	 -{@link InstrMemory#ADDR_SIZE}.
	 <p>	If not valid, adds to the {@link ErrorLog}.
	 
	 @see ErrorLog
	 @see InstrMemory#BASE_INSTR_ADDRESS
	 @see InstrMemory#OVER_INSTR_ADDRESS
	 */
	static boolean isValidInstrAddr(int address, ErrorLog errorLog){
		if (address>=InstrMemory.BASE_INSTR_ADDRESS && address<=InstrMemory.OVER_INSTR_ADDRESS-4)
			return true;
		
		errorLog.append("Instruction Address: \""+Convert.uInt2Hex(address)+"\" Not Valid!");
		return false;
	}
	
	/**
	 Checks if the address, is a Supported Address for Instructions.
	 <p>And can be used with {@link InstrMemory}, after using {@link #addr2index(int, ErrorLog)}.
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
	public static boolean isSupportedInstrAddr(int address, ErrorLog errorLog){
		if (!isValidInstrAddr(address, errorLog))
			return false;
		if (address<=InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS-InstrMemory.ADDR_SIZE)
			return true;
		
		errorLog.append("Instruction Address: \""+Convert.uInt2Hex(address)+"\" Not Supported!");
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
	static boolean isValidDataAddr(int address, ErrorLog errorLog){
		if (address%DataMemory.DATA_ALIGN!=0)
			errorLog.append("Data Address: \""+Convert.uInt2Hex(address)+"\" Not DoubleWord Aligned!");
		else if (address>=DataMemory.BASE_DATA_ADDRESS && address<=(DataMemory.OVER_DATA_ADDRESS-DataMemory.DATA_ALIGN))
			return true;
		else
			errorLog.append("Data Address: \""+Convert.uInt2Hex(address)+"\" Not Valid!");
		
		return false;
	}
	
	/**
	 Checks if the address, is a Supported Address for Data.
	 <p>And can be used with {@link DataMemory}, after using {@link #addr2index(int, ErrorLog)}.
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
	public static boolean isSupportedDataAddr(int address, ErrorLog errorLog){
		if (!isValidDataAddr(address, errorLog))
			return false;
		if (address<=DataMemory.OVER_SUPPORTED_DATA_ADDRESS-DataMemory.DATA_ALIGN)
			return true;
		
		errorLog.append("Data Address: \""+Convert.uInt2Hex(address)+"\" Not Supported!");
		return false;
	}
	
	/**
	 Wrapper of {@link Convert#imm2Address(Integer)}
	 <p>
	 First checks it is in range to be converted,
	 if not, returns null
	 */
	@Nullable
	static Integer convertValidImm2Addr(@NotNull Integer imm){
		try {
			return Convert.imm2Address(imm);
		} catch (IllegalArgumentException e) {
			return null;
			
		}
	}
	
	/**
	 Validates the address then converts it to an Index.
	 <p>
	 If not valid, adds to the {@link ErrorLog}, and returns null.
	 <p>
	 Automatically, verifies if it is an instruction address, or data address.
	 
	 @see ErrorLog
	 @see #isSupportedInstrAddr(int, ErrorLog)
	 @see #isSupportedDataAddr(int, ErrorLog)
	 @see Convert#address2Index(Integer)
	 */
	@Nullable
	public static Integer addr2index(int address, ErrorLog errorLog){
		if (address<DataMemory.BASE_DATA_ADDRESS) {
			if (isSupportedInstrAddr(address, errorLog))
				return Convert.address2Index(address);
		} else if (isSupportedDataAddr(address, errorLog))
			return Convert.address2Index(address);
		return null;
	}
}
