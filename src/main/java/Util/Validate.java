package util;

import model.DataType;
import model.instr.Operands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.logs.ErrorLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.Arrays;
import java.util.List;

/**
 Provides validation for data types. And wrappers of {@link Convert}'s methods with validation performed.
 <p>
 <b>Expects all String parameters to be lowercase ! {@link String#toLowerCase()}</b>
 <p>
 Adds appropriate messages to {@link ErrorLog} {@link WarningsLog}.
 <p>
 After using any method, you should check errLog {@link ErrorLog#hasEntries()},
 and exit the application after printing the errLog's contents.
 
 @see Convert
 @see ErrorLog
 @see WarningsLog */
public class Validate{
	public static final int MAX_FILE_LINES = 512;
	public static final int MAX_INSTR_COUNT = 256;
	
	public static final int BASE_INSTR_ADDRESS = 0x00400000;
	public static final int OVER_SUPPORTED_INSTR_ADDRESS = 0x00500000;
	public static final int OVER_INSTR_ADDRESS = 0x10000000;
	
	public static final int BASE_DATA_ADDRESS = 0x10010000;
	public static final int OVER_SUPPORTED_DATA_ADDRESS = 0x10010800;
	public static final int OVER_DATA_ADDRESS = 0x10040000;
	public static final int DATA_ALIGN = 8;
	//OVER_SUPPORTED_INSTR_ADDRESS and OVER_SUPPORTED_DATA_ADDRESS differ.
	// jumping to an inst address greater than the arbitrary limit, should auto Exit.
	// trying to read/write to a data address great arbitrary limit should give an error.
	
	//TODO make this auto generate based on {@link DataType}
	private static final String SUPPORTED_DATATYPE_CSV = (".word");
	private static final String SUPPORTED_DIRECTIVES_CSV = (".data, .text, .code"+","+SUPPORTED_DATATYPE_CSV);
	//TODO refactor to Enum? or, Loop Up Table ?
	
	// Operands should only belong to one subset, the subsets can then be merged
	private static final String NO_OPERANDS_OPCODE = ("exit, halt");
	private static final String R_RD_RS_RT = ("add, sub");
	
	private static final String I_MEM_READ = ("sw");
	private static final String I_MEM_WRITE = ("lw");
	
	//private static final String I_TYPE_BRANCH = ("branch");
	// if it supports I type labels
	private static final String I_TYPE_RT_IMM_RS = (I_MEM_READ+","+I_MEM_WRITE);
	private static final String I_TYPE_RT_LABEL_DATA = I_TYPE_RT_IMM_RS; // + Branch
	private static final String I_TYPE_RT_RS_IMM = ("addi"); //+Branch
	
	// SUPPORTED OPCODES
	private static final String SUPPORTED_R_TYPE_OPCODE_CSV = (R_RD_RS_RT);
	private static final String SUPPORTED_I_TYPE_OPCODE_CSV = (I_TYPE_RT_RS_IMM+","+I_TYPE_RT_IMM_RS);
	private static final String SUPPORTED_J_TYPE_OPCODE_CSV = ("j, jal");
	// ALL SUPPORTED OPCODES
	private static final String SUPPORTED_OPCODES_CSV =
			SUPPORTED_R_TYPE_OPCODE_CSV+","+SUPPORTED_I_TYPE_OPCODE_CSV+","+SUPPORTED_J_TYPE_OPCODE_CSV+","
					+NO_OPERANDS_OPCODE;
	
	private final ErrorLog errLog;
	
	
	//TODO is Warnings Log necessary ??
	public Validate(ErrorLog errLog){
		this.errLog = errLog;
	}
	
	//TODO -address2index {isSupportedInstrAddr and isSupportedDataAddr}, are the only things used at Execution_Runtime
	// Migrate Validation methods to static ?, only resource used is ErrorLog
	
	/**
	 {@link #BASE_INSTR_ADDRESS} <b>>= address <=</b> {@link #OVER_INSTR_ADDRESS}-4.
	 <p>
	 If not valid, adds to the {@link #errLog}.
	 
	 @see ErrorLog
	 @see #BASE_INSTR_ADDRESS
	 @see #OVER_INSTR_ADDRESS
	 */
	private boolean isValidInstrAddr(int address){
		if (address>=BASE_INSTR_ADDRESS && address<=OVER_INSTR_ADDRESS-4)
			return true;
		
		errLog.append("Instruction Address: \""+Convert.uInt2Hex(address)+"\" Not Valid!");
		return false;
	}
	
	/**
	 Checks if the address, is a Supported Address for Instructions.
	 <p>- and can be used with {@link model.components.InstrMemory}, after using {@link #addr2index(int)}.
	 <p>
	 {@link #BASE_INSTR_ADDRESS} <b>>= address <=</b> {@link #OVER_SUPPORTED_INSTR_ADDRESS}-4.
	 
	 <p>	If not supported, adds to the {@link #errLog}.
	 
	 <p> Prints a different error message, if the address is also not valid. using {@link #isValidInstrAddr(int)}
	 
	 @see ErrorLog
	 @see #isValidInstrAddr(int)
	 @see #BASE_INSTR_ADDRESS
	 @see #OVER_SUPPORTED_INSTR_ADDRESS
	 */
	public boolean isSupportedInstrAddr(int address){
		if (!isValidInstrAddr(address))
			return false;
		if (address<=OVER_SUPPORTED_INSTR_ADDRESS-4)
			return true;
		
		errLog.append("Instruction Address: \""+Convert.uInt2Hex(address)+"\" Not Supported!");
		return false;
	}
	
	/**
	 {@link #BASE_DATA_ADDRESS} <b>>= address <=</b> {@link #OVER_DATA_ADDRESS}-DATA_ALIGN.
	 <p>
	 If not valid, adds to the {@link #errLog}.
	 
	 @see ErrorLog
	 @see #BASE_DATA_ADDRESS
	 @see #OVER_DATA_ADDRESS
	 */
	private boolean isValidDataAddr(int address){
		if (address>=BASE_DATA_ADDRESS && address<=(OVER_DATA_ADDRESS-DATA_ALIGN))
			return true;
		
		errLog.append("Data Address: \""+Convert.uInt2Hex(address)+"\" Not Valid!");
		return false;
	}
	
	/**
	 Checks if the address, is a Supported Address for Data.
	 <p>- and can be used with {@link model.components.DataMemory}, after using {@link #addr2index(int)}.
	 <p>
	 {@link #BASE_DATA_ADDRESS} <b>>= address <=</b>  {@link #OVER_SUPPORTED_DATA_ADDRESS}-DATA_ALIGN.
	 
	 <p>	If not supported, adds to the {@link #errLog}.
	 
	 <p> Prints a different error message, if the address is also not valid. using {@link #isValidDataAddr(int)}
	 
	 @see ErrorLog
	 @see #isValidDataAddr(int)
	 @see #BASE_INSTR_ADDRESS
	 @see #OVER_SUPPORTED_DATA_ADDRESS
	 */
	public boolean isSupportedDataAddr(int address){
		if (!isValidDataAddr(address))
			return false;
		if (address<=OVER_SUPPORTED_DATA_ADDRESS-DATA_ALIGN)
			return true;
		
		errLog.append("Data Address: \""+Convert.uInt2Hex(address)+"\" Not Supported!");
		return false;
	}
	
	/**
	 Wrapper of {@link Convert#imm2Address(Integer)}
	 <p>
	 First checks it is in range to be converted,
	 if not, returns null
	 */
	@Nullable
	Integer convertValidImm2Addr(@NotNull Integer imm){
		try {
			return Convert.imm2Address(imm);
		} catch (IllegalArgumentException e) {
			return null;
			
		}
	}
	
	/**
	 Validates the address then converts it to an Index.
	 <p>
	 If not valid, adds to the {@link #errLog}, and returns null.
	 <p>
	 Automatically, verifies if it is an instruction address, or data address.
	 
	 @see ErrorLog
	 @see #isSupportedInstrAddr(int)
	 @see #isSupportedDataAddr(int)
	 @see Convert#address2Index(Integer)
	 */
	@Nullable
	public Integer addr2index(int address){
		if (address<BASE_DATA_ADDRESS) {
			if (isSupportedInstrAddr(address))
				return Convert.address2Index(address);
		} else if (isSupportedDataAddr(address))
			return Convert.address2Index(address);
		return null;
	}
	
	// END of Address related validation -----------------------------------------------------------------
	
	/**
	 If not valid, adds to the {@link #errLog}.
	 <p>	see README for list of supported directive.
	 
	 @see ErrorLog
	 */
	public boolean isValidDirective(int lineNo, @NotNull String directive){
		boolean rtn = Arrays.asList(
				Convert.splitCSV(SUPPORTED_DIRECTIVES_CSV))
				.contains(directive);
		if (!rtn)
			errLog.append("LineNo: "+lineNo+"\tDirective: \""+directive+"\" Not Supported!");
		
		return rtn;
	}
	
	/**
	 If not valid, adds to the {@link #errLog}.
	 <p>	see README for valid data types.
	 
	 @see ErrorLog
	 */
	public boolean isValidDataType(int lineNo, @NotNull String dataType){
		boolean rtn = Arrays.asList(
				Convert.splitCSV(SUPPORTED_DATATYPE_CSV))
				.contains(dataType);
		if (!rtn)
			errLog.append("LineNo: "+lineNo+"\tDataType: \""+dataType+"\" Not Supported!");
		
		return rtn;
	}
	
	/**
	 If not valid, adds to the {@link #errLog}.
	 <p>	see README for valid label definition.
	 
	 @see ErrorLog
	 */
	public boolean isValidLabel(int lineNo, @NotNull String label){
		if (label.matches("[_a-z][_.\\-a-z\\d]*"))
			return true;
		
		errLog.append("LineNo: "+lineNo+"\tLabel: \""+label+"\" Not Supported!");
		return false;
	}
	
	/**
	 If not valid, adds to the {@link #errLog}
	 <p>	see README for list of supported opcodes.
	 
	 @see ErrorLog
	 */
	boolean isValidOpCode(int lineNo, @NotNull String opcode){
		boolean rtn = Arrays.asList(
				Convert.splitCSV(SUPPORTED_OPCODES_CSV))
				.contains(opcode);
		if (!rtn)
			errLog.append("LineNo: "+lineNo+"\tOpcode: \""+opcode+"\" Not Supported!");
		
		return rtn;
	}
	
	/**
	 First calls {@link #isValidOpCode(int, String)}, Then
	 <p>
	 Calls the appropriate validation method from {@link Val_Operands}
	 And adds the necessary error messages
	 
	 <li>First, checks operands spacing syntax</li>
	 <li>Then, splits the operands</li>
	 <li>Then, Checks the operands, individually in order</li>
	 
	 @param opcode Instruction opcode, used to determine Operands format.
	 //TODO R_type with shift amount uses [0, RT, RD, ShiftAmt]
	 
	 @return <li>Rtype int[3] {RS, RT, RD}</li>
	 <li>Itype int[3] {RS, RT, Imm}</li>
	 <li>Jtype int[3] {}-1, -1, Address}</li>
	 */
	@Nullable
	public Operands splitValidOperands(int lineNo, @NotNull String opcode, String operands, @NotNull WarningsLog warningsLog){
		final String operand_DelimiterRegex = ",\\s*";
		final String openBracket_Regex = "-?\\d*\\s?\\(.+";
		
		if (!isValidOpCode(lineNo, opcode))
			return null;
		// TODO Move operand validation to {@link Operands}
		// NO Op - should be null
		Operands rtn = null;
		DataType type = DataType.NORMAL; // TODO check datatype before adding floating point support
		Integer rd, rs, rt, imm;
		String label;
		
		// No_Operands type
		if (operands==null || operands.isBlank()) {
			if (doesCSVContain(NO_OPERANDS_OPCODE, opcode))
				rtn = Operands.getExit(); // Return Exit Operands (Blank)
			else
				errLog.append("LineNo "+lineNo+"\tNo Operands found!");
			
		} else if (!doesCSVContain(NO_OPERANDS_OPCODE, opcode)) {    // Remainder of types require operands
			//Split operands
			List<String> ops_List = Arrays.asList(operands.split(operand_DelimiterRegex));
			
			switch (ops_List.size()) {
				case 1:
					if (doesCSVContain(SUPPORTED_J_TYPE_OPCODE_CSV, opcode))
						return Jump_LabelOrInt(lineNo, opcode, ops_List);
					break;
				
				case 2:
					if (doesCSVContain(I_TYPE_RT_LABEL_DATA, opcode)) {
						if (doesCSVContain(I_MEM_WRITE, opcode)) //Set RT
							rt = Val_Operands.convertWriteRegister(lineNo, ops_List.get(0), type, errLog, warningsLog);
						else if (doesCSVContain(I_MEM_READ, opcode))
							rt = Val_Operands.convertRegister(lineNo, ops_List.get(0), type, errLog);
						else
							rt = null;
						
						if (rt!=null) { // _ IMM(RS) or _ IMM/LABEL
							if (ops_List.get(1).matches(openBracket_Regex)) // IMM(RS)
								rtn = rt_ImmRs(lineNo, opcode, rt, ops_List.get(1));
							
							else if (doesCSVContain(I_TYPE_RT_LABEL_DATA, opcode)) {  //_ IMM/LABEL
								label = ops_List.get(1);
								rtn = Mem_LabelOrInt(lineNo, opcode, rt, label);
							}
							// TODO Branch RT_RS_IMM
							// RS_IMM
						}
					}
					break;
				
				case 3:    // RD, RS, RT or // RT, RS, IMM
					Integer first = Val_Operands.convertWriteRegister(lineNo, ops_List.get(0), type, errLog, warningsLog);
					Integer second = Val_Operands.convertRegister(lineNo, ops_List.get(1), type, errLog);
					
					if (doesCSVContain(SUPPORTED_R_TYPE_OPCODE_CSV, opcode)) {
						rd = first;
						rs = second;  // RD, RS, RT
						rt = Val_Operands.convertRegister(lineNo, ops_List.get(2), type, errLog);
						if (rd!=null && rs!=null && rt!=null)
							return new Operands(rs, rt, rd);
						
					} else if (doesCSVContain(SUPPORTED_I_TYPE_OPCODE_CSV, opcode)) {
						rt = first;
						rs = second; // RT, RS, IMM
						imm = Val_Operands.convertInteger(lineNo, ops_List.get(2), errLog);
						if (rt!=null && rs!=null && (Val_Operands.is16Bit(imm, lineNo, errLog)!=null))
							return new Operands(opcode, rs, rt, imm);
					}
					break;
				default:
			}
		}
		
		if (rtn==null)
			errLog.append("LineNo: "+lineNo+"\tOperands: ["+operands+"] for Opcode: \""+opcode+"\" Not Valid !");
		return rtn;
	}
	
	@Nullable
	private Operands rt_ImmRs(int lineNo, @NotNull String opcode, @NotNull Integer rt, @NotNull String immRs){
		final String openBracket_Regex = "-?\\d*\\s?\\(.+";
		final String closeBracket_Regex = ".+\\)";
		final String openBracket_DelimiterRegex = "\\s*\\(\\s*";
		final String closeBracket_DelimiterRegex = "\\s*\\)\\s*";
		final int zero = 0;
		
		Integer rs, imm;
		DataType type = DataType.NORMAL;
		if (doesCSVContain(I_TYPE_RT_IMM_RS, opcode))
			if (immRs.matches(openBracket_Regex))
				// check for close bracket
				if (immRs.matches(closeBracket_Regex)) {
					// split into Imm, and rs
					String[] temp = immRs.split(openBracket_DelimiterRegex);
					temp[1] = temp[1].split(closeBracket_DelimiterRegex)[0]; // remove ')' (last symbol)
					
					rs = Val_Operands.convertRegister(lineNo, temp[1], type, errLog);
					if (rs!=null) {
						if (temp[0].isBlank()) // is no Immediate found, set to 0
							imm = zero; // Imm default 0
						else
							imm = Val_Operands.is16Bit(Val_Operands.convertInteger(lineNo, temp[0], errLog),
									lineNo, errLog);
						
						if (imm!=null)
							return new Operands(opcode, rs, rt, imm);
					}
					
				} else
					errLog.append("LineNo: "+lineNo+"\tMissing closing bracket: \")\" !");
		
		return null;
	}
	
	
	private boolean doesCSVContain(@NotNull String CSV, @NotNull String contain){
		return Arrays.asList(Convert.splitCSV(CSV)).contains(contain);
	}
	
	@Nullable
	private Operands Mem_LabelOrInt(int lineNo, @NotNull String opcode, @NotNull Integer rt, @NotNull String addr){
		final int zero = 0;
		
		// Either Label/ Immediate
		if (Val_Operands.isValidLabelOrInt(lineNo, addr, this, errLog)) {
			if (Val_Operands.isLabel(addr)) // if Label - ^ isValidLabelOrInt does deeper check
				return new Operands(opcode, rt, addr);
			// else
			Integer imm = Val_Operands.convertInteger(lineNo, addr, errLog);
			if (imm!=null) {
				Integer address = convertValidImm2Addr(imm);
				if (address!=null && isValidDataAddr(address))
					return new Operands(opcode, zero, rt, imm);
			}
		}
		return null;
	}
	
	private Operands Jump_LabelOrInt(int lineNo, @NotNull String opcode, @NotNull List<String> operands_list){
		if (operands_list.size()==1) {
			String addr = operands_list.get(0);
			Integer imm;
			
			// Either Label/ Immediate
			if (Val_Operands.isValidLabelOrInt(lineNo, addr, this, errLog)) {
				if (Val_Operands.isLabel(addr)) // if Label - ^ isValidLabelOrInt does deeper check
					return new Operands(opcode, addr);
				// else
				imm = Val_Operands.convertInteger(lineNo, addr, errLog);
				if (imm!=null) {
					Integer address = convertValidImm2Addr(imm);
					if (address!=null && isValidInstrAddr(address))
						return new Operands(opcode, imm);
				}
				
			}
		}
		return null;
	}
}

/**
 InnerClass of Validate, to separate operands validation logic
 Used via isValidOperands
 */
class Val_Operands{
	/**
	 <b>See README for supported registers.</b>
	 
	 @return Index in {@link model.components.RegisterBank} the registers value is located at.
	 <p>
	 or NULL for invalid, and adds to the {@link ErrorLog}
	 
	 @see ErrorLog
	 @see WarningsLog
	 */
	@Nullable
	static Integer convertRegister(int lineNo, @NotNull String register, @NotNull DataType dataType, @NotNull ErrorLog errLog){
		// Check if it has a $, and remove it.
		String temp;
		Integer value = null;
		if (dataType==DataType.NORMAL) {
			if (!isInt(register)) {
				if (register.matches("\\$.*") & !register.isBlank())
					temp = register.substring(1);    // Strips $
				else
					temp = register;
				
				if (isInt(temp)) { // if it matches index
					value = Integer.parseInt(temp);
				} else {
					try {
						// 1 - try making Named to R, if it fails assume/ attempt R to Index
						// 2 - if that also fails, then Register = not valid.
						
						try { // Try converting Named to R
							temp = Convert.named2R(temp);
						} catch (IllegalArgumentException e) {
							// Not Valid Named type   -- Attempt R type instead
						}
						value = Convert.r2Index(temp);
					} catch (IllegalArgumentException e) {    // Register is not Named, or R, or index
						errLog.append("LineNo: "+lineNo+"\tRegister: \""+register+"\" Not Valid!");
					}
				}
				
				if (value!=null && (value<0 || value>31)) {// Check Range
					errLog.append("LineNo: "+lineNo+"\tRegister: \""+register+"\" Not In Range!");
					return null;
				}
			} else {
				errLog.append("LineNo: "+lineNo+"\tRegister: \""+register+"\" Not Recognised!");
			}
		} else {
			errLog.append("LineNo: "+lineNo+"\tRegister: \""+register+"\" Wrong DataType!");
		}
		
		return value;
	}
	
	/**
	 Superset of {@link #convertRegister(int, String, DataType, ErrorLog)}, but also checks if the register is
	 index '0' and
	 adds to the {@link WarningsLog}, that writing to 0 is ignored.
	 <p>
	 <b>See README for supported registers.</b>
	 
	 @return Index in {@link model.components.RegisterBank} the registers value is located at.
	 <p>
	 or NULL for invalid, and adds to the {@link ErrorLog}
	 
	 @see ErrorLog
	 @see WarningsLog
	 @see #convertRegister(int, String, DataType, ErrorLog)
	 */
	@Nullable
	static Integer convertWriteRegister(int lineNo, @NotNull String register, @NotNull DataType dataType,
										@NotNull ErrorLog errLog, @NotNull WarningsLog warnLog){
		Integer value = convertRegister(lineNo, register, dataType, errLog);
		if (value!=null && value==0)
			warnLog.append("LineNo: "+lineNo+"\tDestination Register: \""+register+"\" Cannot be modified!,"+
					"\t Result will be ignored!");
		return value;
	}
	
	/**
	 If not valid, adds to the {@link ErrorLog}.
	 <p>	see README for valid Address/Label operand.
	 TODO clean up ! - make static versions of Validate methods that are used by other parts
	 <p>
	 Checks if the value is a valid Label Or Immediate.
	 <p>	<b>Does not check if immediate is a valid address!</b>
	 
	 @see Logger
	 */
	static boolean isValidLabelOrInt(int lineNo, @NotNull String address, @NotNull Validate validate,
									 @NotNull ErrorLog log){
		if (isLabel(address))
			return validate.isValidLabel(lineNo, address);
		
		Integer integer = convertInteger(lineNo, address, log);
		return integer!=null;
	}
	
	
	/**
	 //TODO
	 convertInteger -> is16Bit
	 
	 @see Logger
	 */
	@Nullable
	static Integer convertInteger(int lineNo, @NotNull String immediate, @NotNull ErrorLog errLog){
		Integer imm = null;
		try {    // try hex
			if (isHex(immediate))
				imm = Convert.hex2uInt(immediate);
			else if (isInt(immediate))
				imm = Integer.parseInt(immediate);
		} catch (Exception e) {
			// is not an normal integer -- Imm stays null
		}
		if (imm==null) {
			errLog.append("LineNo: "+lineNo+"\tImmediate: \""+immediate+"\" Not Valid Integer!");
			return null;
		}
		return imm;
	}
	
	/**
	 //TODO
	 Checks if immediate is valid 16 bit value, skips check if null input
	 */
	@Nullable
	public static Integer is16Bit(@Nullable Integer immediate, int lineNo, @NotNull ErrorLog errLog){
		final int IMM_MAX = 32767;
		final int IMM_MIN = -32768;
		if (immediate!=null) {
			if (immediate>=IMM_MIN && immediate<=IMM_MAX)
				return immediate;
			
			errLog.append("LineNo: "+lineNo+"\tImmediate: \""+immediate+"\" Not In Range");
		}
		return null;
	}
	
	private static boolean isInt(String string){
		return string.matches("-?\\d+");
	}
	
	private static boolean isHex(String string){
		return string.matches("0x.*");
	}
	//------ Check Format ----------------
	
	static boolean isLabel(String string){
		return string.matches("[_a-z].*");//[_a-z][_.\-a-z\d]*
	}
}