/******************************************************************************
 * MIT License                                                                *
 *                                                                            *
 * Copyright (c) 2020 Adnaan Hussain                                          *
 *                                                                            *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  *
 * copies of the Software, and to permit persons to whom the Software is      *
 * furnished to do so, subject to the following conditions:                   *
 *                                                                            *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.                            *
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,   *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE*
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER     *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.                                                                  *
 ******************************************************************************/

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
	public static final int MAX_FILE_LINES = 512; //2^8
	
	//TODO make this auto generate based on {@link DataType} - perhaps a HashMap?
	private static final String SUPPORTED_DATATYPE_CSV = (".word");
	private static final String SUPPORTED_DIRECTIVES_CSV = (".data, .text, .code");
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
	
	//TODO , CSV's should be split at initialization
	
	private final ErrorLog errLog;
	
	public Validate(ErrorLog errLog){
		this.errLog = errLog;
	}
	
	/**
	 If not valid, adds to the {@link #errLog}.
	 <p>	see README for list of supported directives. and DataTypes.
	 
	 @see ErrorLog
	 */
	public boolean isValidDirective(int lineNo, @NotNull String directive){
		boolean rtn = (doesCSVContain(SUPPORTED_DIRECTIVES_CSV, directive) || isDataType(directive));
		
		if (!rtn)
			errLog.append("LineNo: "+lineNo+"\tDirective: \""+directive+"\" Not Supported!");
		
		return rtn;
	}
	
	public boolean isDataType(@NotNull String dataType){
		return (doesCSVContain(SUPPORTED_DATATYPE_CSV, dataType));
	}
	
	
	/**
	 If not valid, adds to the {@link #errLog}.
	 <p>	see README for valid label definition.
	 
	 @see ErrorLog
	 */
	@Nullable
	public String isValidLabel(int lineNo, @Nullable String label){
		if (label!=null) {
			if (label.matches("[_a-z][_.\\-a-z\\d]*"))
				return label;
			
			errLog.append("LineNo: "+lineNo+"\tLabel: \""+label+"\" Not Supported!");
		}
		return null;
	}
	
	/**
	 If not valid, adds to the {@link #errLog}
	 <p>	see README for list of supported opcodes.
	 
	 @see ErrorLog
	 */
	public boolean isValidOpCode(int lineNo, @NotNull String opcode){
		if (!Arrays.asList(Convert.splitCSV(SUPPORTED_OPCODES_CSV)).contains(opcode)) {
			errLog.append("LineNo: "+lineNo+"\tOpcode: \""+opcode+"\" Not Supported!");
			return false;
		}
		return true;
	}
	
	/**
	 First calls {@link #isValidOpCode(int, String)}, Then
	 <p>
	 Calls the appropriate validation method from {@link Val_Operands}
	 And adds the necessary error messages
	 
	 <p><b>If Returned Object is Null, Then there are errors with the OpCode, or Operands</b></p>
	 
	 <li>First, checks operands spacing syntax</li>
	 <li>Then, splits the operands</li>
	 <li>Then, Checks the operands, individually in order</li>
	 
	 @param opcode Instruction opcode, used to determine Operands format.
	 //TODO R_type with shift amount uses [0, RT, RD, ShiftAmt]
	 
	 @return <li>R_type int[3] {RS, RT, RD}</li>
	 <li>I_type int[3] {RS, RT, Imm}</li>
	 <li>J_type int[3] {}-1, -1, Address}</li>
	 <li>Exit * <-null-></li>
	 
	 @see #isValidOpCode(int, String)
	 */
	@Nullable
	public Operands splitValidOperands(int lineNo, @Nullable String opcode, String operands, @NotNull WarningsLog warningsLog){
		final String operand_DelimiterRegex = ",\\s*";
		final String openBracket_Regex = "-?\\d*\\s?\\(.+";
		
		if (opcode==null)
			return null;
		// TODO Move operand validation to {@link Operands}?
		// NO Op - should be null
		Operands rtn = null;
		DataType type = DataType.NORMAL; // TODO datatype check before adding floating point support
		Integer rd, rs, rt, imm;
		String label;
		
		// No_Operands type
		if (operands==null || operands.isBlank()) {
			if (doesCSVContain(NO_OPERANDS_OPCODE, opcode))
				rtn = Operands.getExit(); // Return Exit Operands (Blank)
			else
				errLog.append("LineNo "+lineNo+"\t\tNo Operands found!");
			
		} else if (!doesCSVContain(NO_OPERANDS_OPCODE, opcode)) {    // Remainder of types require operands
			//Split operands
			List<String> ops_List = Arrays.asList(operands.split(operand_DelimiterRegex));
			
			switch (ops_List.size()) {
				case 1:
					if (doesCSVContain(SUPPORTED_J_TYPE_OPCODE_CSV, opcode))
						rtn = Jump_LabelOrInt(lineNo, opcode, ops_List);
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
					errLog.append("LineNo: "+lineNo+"\t\tMissing closing bracket: \")\" !");
		
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
				Integer address = AddressValidation.convertValidImm2Addr(lineNo, imm, errLog);
				if (address!=null && AddressValidation.isSupportedDataAddr(address, errLog))
					return new Operands(opcode, zero, rt, imm);
			}
		}
		return null;
	}
	
	/**
	 Immediate values are shifted to be converted into addresses. Then the address is checked to be valid.
	 */
	private Operands Jump_LabelOrInt(int lineNo, @NotNull String opcode, @NotNull List<String> operands_list){
		if (operands_list.size()==1) {
			String addr = operands_list.get(0);
			Integer imm;
			
			// Either Label/ Immediate
			if (Val_Operands.isValidLabelOrInt(lineNo, addr, this, errLog)) {
				if (Val_Operands.isLabel(addr)) // if Label - ^ isValidLabelOrInt already does deeper check
					return new Operands(opcode, addr);
				// else
				imm = Val_Operands.convertInteger(lineNo, addr, errLog);
				if (imm!=null) {
					Integer address = AddressValidation.convertValidImm2Addr(lineNo, imm, errLog);
					if (address!=null && AddressValidation.isSupportedInstrAddr(address, errLog))
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
						errLog.append("LineNo: "+lineNo+"\t\tRegister: \""+register+"\" Not Valid!");
					}
				}
				
				if (value!=null && (value<0 || value>31)) {// Check Range
					errLog.append("LineNo: "+lineNo+"\t\tRegister: \""+register+"\" Not In Range!");
					return null;
				}
			} else {
				errLog.append("LineNo: "+lineNo+"\t\tRegister: \""+register+"\" Not Recognised!");
			}
		} else {
			errLog.append("LineNo: "+lineNo+"\t\tRegister: \""+register+"\" Wrong DataType!");
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
			return validate.isValidLabel(lineNo, address)!=null;
		
		return convertInteger(lineNo, address, log)!=null;
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
			errLog.append("LineNo: "+lineNo+"\t\tImmediate Value: \""+immediate+"\" Not Valid Integer!");
			return null; // UNNECESSARY ?
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
			
			errLog.append("LineNo: "+lineNo+"\t\tImmediate Value: \""+immediate+"\" Not In Range");
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