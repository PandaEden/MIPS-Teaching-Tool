package Util;

/**
 Provides validation for data types. And wrappers of {@link Convert}'s methods with validation performed.
 <p>
 Validation Errors/Warnings are added to the {@link Log} log.
 
 After using any method, you should check {@link Log#hasErrors()}, and exit the application after printing the ErrorLog.
 
 @see Convert
 @see Log */
public class Validate{
	
	public Validate(Log log){
	}
	
	/**
	 "0x0040000" {@link #BASE_INSTR_ADDRESS} >= address <= {@link #MAX_INSTR_ADDRESS}.
	 <p>
	 If not valid, adds an Error to the log.
	 
	 @see Log
	 @see #BASE_INSTR_ADDRESS
	 @see #MAX_INSTR_ADDRESS
	 */
	public boolean isValidInstrAddr(int address){
		return false;
	}
	
	/**
	 "0x0040000" {@link #BASE_DATA_ADDRESS} >= address <= {@link #MAX_DATA_ADDRESS}.
	 <p>
	 If not valid, adds an Error to the log.
	 
	 @see Log
	 @see #BASE_DATA_ADDRESS
	 @see #MAX_DATA_ADDRESS
	 */
	public boolean isValidDataAddr(int address){
		return false;
	}
	
	/**
	 Validates the address then converts it to an Index.
	 <p>
	 If not valid, adds an Error to the log.
	 
	 @see Log
	 @see #isValidInstrAddr(int)
	 @see #isValidDataAddr(int)
	 @see Convert#address2index(int)
	 */
	public int addr2index(int address){
		return -1;
	}
	
	/**
	 If not valid, adds an Error to the log.
	 
	 @see Log
	 */
	public boolean isValidOpCode(String opcode){
		return false;
	}
	
	/**
	 If not valid, adds an Error to the log.
	 
	 @see Log
	 */
	public boolean isValidDirective(String directive){
		return false;
	}
	
	/**
	 If not valid, adds an Error to the log.
	 @see Log
	 */
	public boolean isValidLabel(String label){
		return false;
	}
	
	/**
	 Checks the register name is valid R style or Named reference.
	 <p>
	 "$ before register reference is optional", case-insensitive.
	 
	 <li>R_style - "R0, R1. ... R31"</li>
	 <li>Named - "Zero, S0-8, T0-9"</li>
	 <p>
	 if not, adds to ErrorLog
	 
	 @see Log
	 */
	public boolean isValidRegister(String register){
		return false;
	}
	
	/**
	 Using {@link #isValidRegister(String)}, checks if register name is valid.
	 <p>
	 If it is, returns the Index in {@link RegisterBank} the registers value is located at.
	 <p>
	 or -1 for invalid, and adds to ErrorLog
	 
	 @see Log
	 @see #isValidRegister(String)
	 */
	public int convertValidRegister(String register){
		return -1;
	}
	
	/**
	 Superset of {@link #isValidRegister(String)} but also prints a warning if trying to write to (0, $zero, R0)
	 register.
	 
	 @see Log
	 @see #isValidRegister(String)
	 @see #convertValidRegister(String)
	 */
	public boolean isValidWriteRegister(String register){
		return false;
	}
}
