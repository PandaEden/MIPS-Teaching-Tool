package util.validation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import model.DataType;
import model.instr.Operands;

import util.Convert;
import util.logs.ErrorLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperandsValidation {
	// Operands should only belong to one subset, the subsets can then be merged
	static final List<String> NO_OPERANDS_OPCODE = List.of("exit", "halt");
	private static final List<String> R_RD_RS_RT = List.of("add", "sub");
	
	private static final List<String> I_MEM_READ =  List.of("sw");
	private static final List<String> I_MEM_WRITE =  List.of("lw");
	
	//private static final List<String> I_TYPE_BRANCH =  List.of("branch");
	// if it supports I type labels
	private static final List<String> I_TYPE_RT_IMM_RS = Stream.of( I_MEM_READ, I_MEM_WRITE )
															   .flatMap( Collection::stream ).collect( Collectors.toList( ) );
	private static final List<String> I_TYPE_RT_LABEL_DATA = I_TYPE_RT_IMM_RS; // + Branch
	private static final List<String> I_TYPE_RT_RS_IMM = List.of("addi"); //+Branch
	
	// SUPPORTED OPCODES
	static final List<String> SUPPORTED_R_TYPE_OPCODE_CSV = (R_RD_RS_RT);
	static final List<String> SUPPORTED_I_TYPE_OPCODE_CSV = Stream.of(I_TYPE_RT_RS_IMM, I_TYPE_RT_IMM_RS )
																		  .flatMap(Collection::stream ).collect(Collectors.toList( ) );
	static final List<String> SUPPORTED_J_TYPE_OPCODE_CSV = List.of("j", "jal");
	
	private final ErrorLog errorLog;
	private final WarningsLog warningsLog;
	private String opcode = "";
	public OperandsValidation(@NotNull ErrorLog errorLog,@NotNull WarningsLog warningsLog) {
		this.errorLog=errorLog;
		this.warningsLog=warningsLog;
	}
	
	/**
	 First calls {@link Validate#isValidOpCode(int, String)}, Then
	 <p>
	 Validates the operands are valid for their format.
	 
	 <p><b>If Returned Object is Null, Then there are errors Operands, Or the Opcode is Null</b></p>
	 
	 <li>First, checks operands spacing syntax</li>
	 <li>Then, splits the operands</li>
	 <li>Then, Checks the operands, individually in order</li>
	 
	 @param opcode Instruction opcode, used to determine Operands format.
	 //TODO R_type with shift amount uses [0, RT, RD, ShiftAmt]
	 
	 @return <li>R_type int[3] {RS, RT, RD}</li>
	 <li>I_type int[3] {RS, RT, Imm}</li>
	 <li>J_type int[3] {}-1, -1, Address}</li>
	 <li>Exit * <-null-></li>
	 
	 @see Validate#isValidOpCode(int, String)
	 */
	@Nullable
	public Operands splitValidOperands(int lineNo, @Nullable String opcode, String operands){
		final String operand_DelimiterRegex = "\\s?,\\s?";
		final String openBracket_Regex = "-?(0x)?\\d*\\s?\\(.+";
		
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
			if (NO_OPERANDS_OPCODE.contains( opcode))
				rtn = Operands.getExit(); // Return Exit Operands (Blank)
			else
				errorLog.appendEx("LineNo: "+lineNo+"\t\tNo Operands found");
			
		} else if (!NO_OPERANDS_OPCODE.contains(opcode)) {    // Remainder of types require operands
			//Split operands
			List<String> ops_List = Arrays.asList( operands.split( operand_DelimiterRegex));
			
			switch (ops_List.size()) {
				case 1:
					if (SUPPORTED_J_TYPE_OPCODE_CSV.contains(opcode))
						rtn = Jump_LabelOrInt(lineNo, opcode, ops_List);
					break;
				
				case 2:
					if (I_TYPE_RT_LABEL_DATA.contains(opcode)) {
						if (I_MEM_WRITE.contains(opcode)) //Set RT
							rt = convertWriteRegister(lineNo, ops_List.get(0), type, errorLog, warningsLog);
						else if (I_MEM_READ.contains(opcode))
							rt = convertRegister(lineNo, ops_List.get(0), type, errorLog);
						else
							rt = null;
						
						if (rt!=null) { // _ IMM(RS) or _ IMM/LABEL
							if (ops_List.get(1).matches(openBracket_Regex)) // IMM(RS)
								rtn = rt_ImmRs(lineNo, opcode, rt, ops_List.get(1));
							
							else if (I_TYPE_RT_LABEL_DATA.contains(opcode)) {  //_ IMM/LABEL
								label = ops_List.get(1);
								rtn = Mem_LabelOrInt(lineNo, opcode, rt, label);
							}
							// TODO Branch RT_RS_IMM
							// RS_IMM
						}
					}
					break;
				
				case 3:    // RD, RS, RT or // RT, RS, IMM
					Integer first = convertWriteRegister(lineNo, ops_List.get(0), type, errorLog, warningsLog);
					Integer second = convertRegister(lineNo, ops_List.get(1), type, errorLog);
					
					if (SUPPORTED_R_TYPE_OPCODE_CSV.contains(opcode)) {
						rd = first;
						rs = second;  // RD, RS, RT
						rt = convertRegister(lineNo, ops_List.get(2), type, errorLog);
						if (rd!=null && rs!=null && rt!=null)
							return new Operands(rs, rt, rd);
						
					} else if (SUPPORTED_I_TYPE_OPCODE_CSV.contains(opcode)) {
						rt = first;
						rs = second; // RT, RS, IMM
						imm = convertInteger(lineNo, ops_List.get(2), errorLog);
						if (rt!=null && rs!=null && (is16Bit(imm, lineNo, errorLog)!=null))
							return new Operands(opcode, rs, rt, imm);
					}
					break;
				default:
			}
		}
		
		if (rtn==null)
			errorLog.appendEx("LineNo: "+lineNo+"\tOperands: ["+operands+"] for Opcode: \""+opcode+"\" Not Valid");
		return rtn;
	}
	
	@Nullable
	private Operands rt_ImmRs(int lineNo, @NotNull String opcode, @NotNull Integer rt, @NotNull String immRs){
		final String openBracket_Regex = "-?(0x)?\\d*\\s?\\(.+";
		final String closeBracket_Regex = ".+\\)";
		final String openBracket_DelimiterRegex = "\\s*\\(\\s*";
		final String closeBracket_DelimiterRegex = "\\s*\\)\\s*";
		final int zero = 0;
		
		Integer rs, imm;
		DataType type = DataType.NORMAL;
		if (I_TYPE_RT_IMM_RS.contains(opcode))
			if (immRs.matches(openBracket_Regex))
				// check for close bracket
				if (immRs.matches(closeBracket_Regex)) {
					// split into Imm, and rs
					String[] temp = immRs.split(openBracket_DelimiterRegex);
					temp[1] = temp[1].split(closeBracket_DelimiterRegex)[0]; // remove ')' (last symbol)
					
					rs = convertRegister(lineNo, temp[1], type, errorLog);
					if (rs!=null) {
						if (temp[0].isBlank()) // is no Immediate found, set to 0
							imm = zero; // Imm default 0
						else
							imm = is16Bit(convertInteger(lineNo, temp[0], errorLog),
													   lineNo, errorLog);
						
						if (imm!=null)
							return new Operands(opcode, rs, rt, imm);
					}
					
				} else
					errorLog.append("LineNo: "+lineNo+"\t\tMissing closing bracket: \")\" !");
		
		return null;
	}
	
	@Nullable
	private Operands Mem_LabelOrInt(int lineNo, @NotNull String opcode, @NotNull Integer rt, @NotNull String addr){
		final int zero = 0;
		
		// Either Label/ Immediate
		if (isValidLabelOrInt(lineNo, addr,  errorLog)) {
			if (isLabel(addr)) // if Label - ^ isValidLabelOrInt does deeper check
				return new Operands(opcode, rt, addr);
			// else
			Integer imm =convertInteger(lineNo, addr, errorLog);
			if (imm!=null) {
				Integer address = AddressValidation.convertValidImm2Addr(lineNo, imm, errorLog);
				if (address!=null && AddressValidation.isSupportedDataAddr(address, errorLog))
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
			if (isValidLabelOrInt(lineNo, addr, errorLog)) {
				if (isLabel(addr)) // if Label - ^ isValidLabelOrInt already does deeper check
					return new Operands(opcode, addr);
				// else
				imm = convertInteger(lineNo, addr, errorLog);
				if (imm!=null) {
					Integer address = AddressValidation.convertValidImm2Addr(lineNo, imm, errorLog);
					if (address!=null && AddressValidation.isSupportedInstrAddr(address, errorLog))
						return new Operands(opcode, imm);
				}
				
			}
		}
		return null;
	}

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
							temp = Convert.named2R( temp);
						} catch (IllegalArgumentException e) {
							// Not Valid Named type   -- Attempt R type instead
						}
						value = Convert.r2Index(temp);
					} catch (IllegalArgumentException e) {    // Register is not Named, or R, or index
						errLog.append("LineNo: "+lineNo+"\t\tRegister: \""+register+"\" Not Recognised!");
					}
				}
				
				if (value!=null && (value<0 || value>31)) {// Check Range
					errLog.append("LineNo: "+lineNo+"\t\tRegister: \""+register+"\" Not In Range!");
					return null;
				}
			} else {	// Index Registers Require '$'
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
	static boolean isValidLabelOrInt(int lineNo, @NotNull String address, @NotNull ErrorLog log){
		if (isLabel(address))
			return Validate.isValidLabel(lineNo, address, log)!=null;
		
		return convertInteger(lineNo, address, log)!=null;
	}
	
	
	/**
	 use with {@link #is16Bit(Integer, int, ErrorLog)}
	 
	 @see Logger
	 */
	@Nullable
	static Integer convertInteger(int lineNo, @NotNull String immediate, @NotNull ErrorLog errLog){
		Integer imm = null;
		try {    // try hex
			if (isHex(immediate))
				imm = Integer.decode( immediate );
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
	 Checks if immediate is valid 16 bit value, skips check if null input
	 */
	@Nullable
	protected static Integer is16Bit(@Nullable Integer immediate, int lineNo, @NotNull ErrorLog errLog) {
		final int IMM_MAX=32767;
		final int IMM_MIN=-32768;
		if ( immediate!=null ) {
			if ( immediate>=IMM_MIN && immediate<=IMM_MAX )
				return immediate;
			
			errLog.append( "LineNo: " + lineNo + "\t\tImmediate Value: \"" + immediate + "\" Not In Range" );
		}
		return null;
	}
	
	private static boolean isInt(String string) {
		return string.matches( "-?\\d+" );
	}
	
	private static boolean isHex(String string) {
		return string.matches( "0x.*" );
	}
	//------ Check Format ----------------
	
	static boolean isLabel(String string) {
		return string.matches( "[_a-z].*" );//[_a-z][_.\-a-z\d]*
	}


	
}
