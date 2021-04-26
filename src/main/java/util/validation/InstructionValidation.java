package util.validation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import model.DataType;
import model.instr.*;

import util.Convert;
import util.Util;
import util.logs.ErrorLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstructionValidation {
	
	// Operands should only belong to one subset, the subsets can then be merged
	public static final List<String> NO_OPERANDS_OPCODE=List.of( "exit", "halt" );
	public static final List<String> R_RD_RS_RT=List.of( "add", "sub" );
	
	// if it supports I type labels
	public static final List<String> I_MEM_READ=List.of( "sw" );
	public static final List<String> I_MEM_WRITE=List.of( "lw" );
	public static final List<String> I_RT_IMM_RS=Stream.of( I_MEM_READ, I_MEM_WRITE )
													   .flatMap( Collection :: stream ).collect( Collectors.toUnmodifiableList( ) );
	
	public static final List<String> I_RT_RS_IMM=List.of( "addi" );
	public static final List<String> I_RS_RT_IMM=List.of( "beq", "bne", "bgt", "blt", "bge", "ble" );
	// SUPPORTED OPCODES
	public static final List<String> R_TYPE=(R_RD_RS_RT);
	public static final List<String> I_TYPE=Stream.of( I_RT_RS_IMM, I_RS_RT_IMM, I_RT_IMM_RS )
												   .flatMap( Collection :: stream ).collect( Collectors.toUnmodifiableList( ) );
	public static final List<String> J_TYPE=List.of( "j", "jal" );
	
	// ALL SUPPORTED OPCODES
	public static final List<String> SUPPORTED_OPCODES=
			Stream.of( R_TYPE, I_TYPE,
					   J_TYPE, NO_OPERANDS_OPCODE )
				  .flatMap( Collection :: stream ).collect( Collectors.toUnmodifiableList( ) );
	
	// TODO Write a helper method with searches all the specs, and creates a list of opcodes for each format.
	
	public static final List<InstrSpec> SPEC = List.of(	// Decoder Input ::  Dest | ALUSrc1|AluSrc2 | AluOp | MemOp|MemToReg | PCWrite|BranchCond
			new InstrSpec( "add", "Addition", 3, InstrSpec.FMT.RD_RS_RT, new Integer[]{ 1, 0,0,0, null,0, 0,null} ),
			new InstrSpec( "sub", "Subtraction", 3, InstrSpec.FMT.RD_RS_RT, new Integer[]{ 1, 0,0,2, null,0, 0,null} ),
			//
			new InstrSpec( "addi", "Addition_Imm", 3, InstrSpec.FMT.RT_RS_IMM, new Integer[]{ 0, 0,1,0, null,0, 0,null} ),
			new InstrSpec( "lw", "Load Word", 2, InstrSpec.FMT.RT_MEM, new Integer[]{ 0, 0,1,0, 0,1, 0,null} ),
			new InstrSpec( "sw", "Store Word", 2, InstrSpec.FMT.RT_MEM, new Integer[]{ null, 0,1,0, 1,null, 0,null} ),
			
			new InstrSpec( "j", "Jump", 1, InstrSpec.FMT.JUMP_ADDR, new Integer[]{ null, null,null,null, null,null, 1,null} ),
			new InstrSpec( "jal", "Jump And Link", 1, InstrSpec.FMT.JUMP_ADDR, new Integer[]{ 2, 1,null,-1, null,0, 1,null} ),
			
			new InstrSpec( "exit", "Syscall-Exit", 0, InstrSpec.FMT.NO_OPS, new Integer[]{ null, null,null,null, null,null, null,null} ),
			new InstrSpec( "halt", "Syscall-Exit", 0, InstrSpec.FMT.NO_OPS, new Integer[]{ null, null,null,null, null,null, null,null} ),
			
			new InstrSpec( "beq", "Branch - on - Equal", 3, InstrSpec.FMT.RS_RT_OFFSET, new Integer[]{ null, 0,0,6, null,null, 2,0} ),
			new InstrSpec( "bne", "Branch - on - NOT~Equal", 3, InstrSpec.FMT.RS_RT_OFFSET, new Integer[]{ null, 0,0,6, null,null, 2,1} ),
			new InstrSpec( "blt", "Branch - on - LessThan", 3, InstrSpec.FMT.RS_RT_OFFSET, new Integer[]{ null, 0,0,8, null,null, 2,1} ),
			new InstrSpec( "bge", "Branch - on - GreaterThan Or Equal", 3, InstrSpec.FMT.RS_RT_OFFSET, new Integer[]{ null, 0,0,8, null,null, 2,0} ),
			new InstrSpec( "ble", "Branch - on - LessThan Or Equal", 3, InstrSpec.FMT.RS_RT_OFFSET, new Integer[]{ null, 0,0,9, null,null, 2,1} ),
			new InstrSpec( "bgt", "Branch - on - GreaterThan", 3, InstrSpec.FMT.RS_RT_OFFSET, new Integer[]{ null, 0,0,9, null,null, 2,0} ),
			
			new InstrSpec( "nop", "NO_OPERATION", 0, InstrSpec.FMT.NO_OPS, new Integer[]{ null, null,null,null, null,null, null,null} )
	);
	
	private final ErrorLog errorLog;
	private final WarningsLog warningsLog;
	private String opcode;
	private int lineNo;
	// TODO - rename to InstructionValidation
	// TODO - Move to Instructions Package, and change visibility
	public InstructionValidation (@NotNull ErrorLog errorLog, @NotNull WarningsLog warningsLog) {
		this.errorLog=errorLog;
		this.warningsLog=warningsLog;
		setLineNo( -1 );
	}
	@VisibleForTesting
	void setLineNo (int lineNo) {
		this.lineNo=lineNo;
	}
	/** @see Util#isNullOrBlank(String) */
	private static boolean isDec (@Nullable String string) {
		return !Util.isNullOrBlank( string ) && string.matches( "-?\\d*(\\d\\.|\\.\\d|\\d)\\d*" );
	}
	/** @see Util#isNullOrBlank(String) */
	private static boolean isHex (@Nullable String string) {
		return !Util.isNullOrBlank( string ) && string.matches( "0x.*" );
	}
	/**
	 If not valid, adds to the {@link #errorLog}
	 <p>	see README for list of supported opcodes.
	 
	 @see ErrorLog
	 */
	@VisibleForTesting
	protected boolean isValidOpCode (int lineNo, @NotNull String opcode) {
		if ( !SUPPORTED_OPCODES.contains( opcode ) ) {
			errorLog.append( "LineNo: " + lineNo + "\tOpcode: \"" + opcode + "\" Not Supported!" );
			return false;
		}
		return true;
	}
	
	/**
	 First calls {@link #isValidOpCode(int, String)}, Then
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
	 
	 @see #isValidOpCode(int, String)
	 */
	@Nullable
	public Instruction splitValidOperands (int lineNo, @Nullable String opcode, @Nullable String operands) {
		final String opsNotValid = "Operands: [" + operands + "] for Opcode: \"" + opcode + "\" Not Valid ";
		final String comma="\\s?,\\s?";
		
		if ( opcode==null )
			return null;
		
		Instruction rtn=null;
		
		if ( isValidOpCode(lineNo, opcode) ) {
			
			setOpcode( opcode );    // Todo refactor into method signatures
			setLineNo( lineNo );
			
			DataType dataType=DataType.NORMAL; // TODO datatype check before adding floating point support
			Integer rs, rt, imm;
			
			// No_Operands type
			if ( Util.isNullOrBlank( operands ) ) {
				if ( NO_OPERANDS_OPCODE.contains( opcode ) )
					rtn=new Nop( opcode );
				else
					errorLog.appendEx( lineNo, "\tNo Operands found" );
				// -> Not Valid
			} else if ( !NO_OPERANDS_OPCODE.contains( opcode ) ) {    // Remainder of types require operands
				if ( operands.contains( "#" ) )
					throw new IllegalStateException( "# Comments Not Removed By Parser!" );
				
				//Split operands
				String first, second=null, third=null;
				List<String> ops_List=Arrays.asList( (operands + "#").split( comma ) );
				// Remove padded #
				if ( ops_List.size( )<2 ) {
					first=trimHash( ops_List.get( 0 ) );
				} else if ( ops_List.size( )<3 ) {
					first=ops_List.get( 0 );
					second=trimHash( ops_List.get( 1 ) );
				} else {
					first=ops_List.get( 0 );
					second=ops_List.get( 1 );
					third=trimHash( ops_List.get( 2 ) );
				}
				switch ( ops_List.size( ) ) {
					case 1:
						if ( J_TYPE.contains( opcode ) )
							rtn=Jump_LabelOrInt( first );
						break;// -> Not Valid
					
					case 2:
						if ( I_RT_IMM_RS.contains( opcode ) ) {        // TODO - Could set InstrType here instead?
							if ( I_MEM_WRITE.contains( opcode ) ) //Set RT
								rt=convertWriteRegister( first, dataType );
							else if ( I_MEM_READ.contains( opcode ) )
								rt=convertRegister( first, dataType );
							else rt=null; // Unreachable
							// Future - (Branch) else rt=null;// TODO Branch RT_RS_IMM /RS_IMM
							
							if ( rt!=null )  // _ IMM(RS) or _ IMM/LABEL
								rtn=rt_ImmRs( rt, second ); // _ IMM($0) or _ IMM/LABEL
						}
						break;// -> Not Valid
					
					case 3:    // RD, RS, RT or // RT, RS, IMM - first Operands is Write Destination
						if ( R_TYPE.contains( opcode ) || I_RT_RS_IMM.contains( opcode ) ) {
							Integer destination=convertWriteRegister( first, dataType );
							rs=convertRegister( second, dataType );
							
							if ( R_TYPE.contains( opcode ) ) {
								// RD, RS, RT
								rt=convertRegister( third, dataType );
								if ( destination!=null && rs!=null && rt!=null )
									rtn=new R_Type( opcode, rs, rt, destination );
							} else if ( I_RT_RS_IMM.contains( opcode ) ) {
								// RT, RS, IMM
								rt=destination;
								imm=is16Bit( convertInteger( third ) );    // Check for Null/Blank
								if ( rt!=null && rs!=null && imm!=null )
									rtn=new I_Type( opcode, rs, rt, imm );
							}
						} else if ( I_RS_RT_IMM.contains( opcode ) ) {
							// RT, RS, INSTR - IMM Offset/Label for Instruction PC
							rs=convertRegister( first, dataType );
							rt=convertRegister( second, dataType );;
							// IMM may be Label
							if ( rs!=null && rt!=null )
								rtn = Branch_LabelOrInt( rs, rt, third );
						}
						break;// -> Not Valid
					default:    // if for some reason the user gives more than 3 operands ? -> not Valid
				}
			}
			if ( rtn==null )
				errorLog.appendEx( lineNo, opsNotValid );
		}
		return rtn;
	}
	
	/** Trims Last Value in string, expected last value is a hash*/
	private String trimHash(String s){return s.substring( 0,s.length()-1 ); }
	
	/**
	 Splits Valid Imm(RS), If they are valid, and correctly formatted,
	 Returns a {@link I_Type} instruction with the operands assigned.
	 */
	@Nullable
	@VisibleForTesting
	protected I_Type rt_ImmRs (@NotNull Integer rt, @Nullable String immRs) {
		final String openBracket_DelimiterRegex="\\s?\\(\\s?";
		final String closeBracket_DelimiterRegex="\\s?\\)\\s?";
		
		Integer rs, imm;
		String rs_String, immediateString;
		String[] split;
		if ( !Util.isNullOrBlank( immRs ) ) {
			if ( immRs.contains( "(" ) ) {// Check for Brackets, if there are - set RS
				if ( immRs.contains( ")" ) ) {// check for close bracket
					// split into Imm, and rs
					split=immRs.split( openBracket_DelimiterRegex, 2 );    // IMM | RS
					immediateString=split[ 0 ];
					
					//Set IMM
					immediateString=Util.isNullOrBlank( immediateString ) ? "0" : immediateString;
					imm=is16Bit( convertInteger( immediateString ) );    // not 16bit, Imm->null
					
					split=split[ 1 ].split( closeBracket_DelimiterRegex, 2 ); // remove ')', expect returned[1] to be null
					rs_String=split[ 0 ];
					
					// Validate RS -> if Null, defaults to $zero
					rs_String=Util.isNullOrBlank( rs_String ) ? "$zero" : rs_String;
					rs=convertRegister( rs_String, DataType.NORMAL );
					
					//TODO check if RS=0, if so, check if IMM is a valid address
					if ( imm!=null && rs!=null )
						return new MemAccess( opcode, rs, rt, imm );    // Return Imm(RS)
					// else -> return null
				} else
					errorLog.appendEx( lineNo, "\tMissing Closing Bracket: \")\" " );
			} else if ( immRs.contains( ")" ) )    // unmatched Bracket
				errorLog.appendEx( lineNo, "\tMissing Opening Bracket: \"(\" " );
			else
				return Mem_LabelOrInt( rt, immRs );    // Return Label/Imm
		} else
			errorLog.appendEx( lineNo, "\t\tNo Imm(RS) found" );
		return null;
	}
	@Nullable
	@VisibleForTesting
	protected MemAccess Mem_LabelOrInt (@NotNull Integer rt, @NotNull String addr) {
		if ( !Util.isNullOrBlank( addr ) ) {
			Integer imm;
			if ( isDec( addr ) || isHex( addr ) ){
				if ( (imm=is16Bit( convertInteger( addr ) ))!=null ) {
					//TODO - This check is skipped with empty brackets Imm(),  Needs to be consistent !
					Integer address=AddressValidation.convertValidImm2Addr( lineNo, imm, errorLog );
					if ( address!=null && AddressValidation.isSupportedDataAddr( address, errorLog ) )
						throw new IllegalStateException(
								"You Have Broken The Laws Of Mathematics, Or I have some Debugging to do!" );//return new Operands( opcode, zero, rt, imm );
				}
			}else if ( isValidLabel( addr ) )
				return new MemAccess( opcode, rt, addr );
			
		}
		return null;
	}
	/**
	 Immediate values are shifted to be converted into addresses. Then the address is checked to be valid.
	 
	 @see Util#isNullOrBlank(String)
	 */
	@Nullable
	@VisibleForTesting
	protected J_Type Jump_LabelOrInt (@Nullable String addr) {
		if ( !Util.isNullOrBlank( addr ) ) {
			Integer imm;
			if ( isDec( addr ) || isHex( addr ) ) {
				if ( (imm=isU26Bit( convertInteger( addr ) ))!=null ) {
					Integer address=AddressValidation.convertValidImm2Addr( lineNo, imm, errorLog );
					if ( address!=null && AddressValidation.isSupportedInstrAddr( address, errorLog ) )
						return new J_Type( opcode, imm );
				}
			} else if ( isValidLabel( addr ) )
					return new J_Type( opcode, addr );
		}
		return null;
	}
	@Nullable
	@VisibleForTesting
	protected I_Type Branch_LabelOrInt (@NotNull Integer rs, @NotNull Integer rt, @Nullable String addr) {
		if ( !Util.isNullOrBlank( addr ) ) {
			Integer imm;
			if ( isDec( addr ) || isHex( addr ) ){
				if ( (imm=is16Bit( convertInteger( addr ) ))!=null ) {
					return new Branch( opcode, rs,rt,imm );
				}
			}else if ( isValidLabel( addr ) )
				return new Branch( opcode, rs,rt,addr );
			
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
	 @see Util#isNullOrBlank(String)
	 */
	@Nullable
	@VisibleForTesting
	protected Integer convertRegister (@Nullable String register, @NotNull DataType dataType) {
		// Check if it has a $, and remove it.
		String temp;
		Integer value=null;
		boolean recognised=false;
		if ( dataType==DataType.NORMAL ) {
			if ( !Util.isNullOrBlank( register ) && !isDec( register ) ) {
				if ( register.matches( "\\$.*" ) & !register.isBlank( ) )
					temp=register.substring( 1 );    // Strips $
				else
					temp=register;
				
				if ( isDec( temp ) ) { // if it matches index
					value=Integer.parseInt( temp );
					recognised=true;
				} else {
					try {
						// 1 - try making Named to R, if it fails assume/ attempt R to Index
						// 2 - if that also fails, then Register = not valid.
						
						// Try converting Named to R
						try { temp=Convert.named2R( temp ); } catch ( IllegalArgumentException ignored ) {}
						// When not Valid Named type   -- Attempt R type instead
						
						value=Convert.r2Index( temp );
						recognised=true;
					} catch ( IllegalArgumentException ignored ) {}// Register is not Named, or R, or index / Not Recognised
				}
			}    // Index without $, - recognised set to false by default
			
			if ( !recognised ) {
				errorLog.append( lineNo, "\tRegister: \"" + register + "\" Not Recognised!" );
			} else if ( !Util.notNullAndInRange( value, 0, 31 ) ) {// Check Range
				errorLog.append( lineNo, "\tRegister: \"" + register + "\" Not In Range!" );
				value=null; // reset Value to null
			}
		} else { errorLog.append( lineNo, "\tRegister: \"" + register + "\" Wrong DataType!" ); }
		
		return value;
	}
	/**
	 Superset of {@link #convertRegister(String, DataType)}, but also checks if the register is
	 index '0' and
	 adds to the {@link WarningsLog}, that writing to 0 is ignored.
	 <p>
	 <b>See README for supported registers.</b>
	 
	 @return Index in {@link model.components.RegisterBank} the registers value is located at.
	 <p>
	 or NULL for invalid, and adds to the {@link ErrorLog}
	 
	 @see ErrorLog
	 @see WarningsLog
	 @see #convertRegister(String, DataType)
	 */
	@Nullable
	@VisibleForTesting
	protected Integer convertWriteRegister (@Nullable String register, @NotNull DataType dataType) {
		Integer value=convertRegister( register, dataType );
		if ( value!=null && value==0 )
			warningsLog.appendEx( lineNo, "Destination Register: \"" + register + "\" Cannot be modified!," +
										  "\t Result will be ignored" );
		return value;
	}
	
	/** Wrapper for {@link Validate#isValidLabel(int, String, ErrorLog)}*/
	boolean isValidLabel (@Nullable String address){
		return (Validate.isValidLabel(lineNo, address, errorLog)!=null);
	}
	
	
	/**
	 use with {@link #is16Bit(Integer)}
	 <li>ifHex -> converts to Int</li>
	 <li>ifInt -> parseInt</li>
	 <p>
	 Adds to {@link #errorLog} if not Valid Integer
	 
	 @see Logger
	 @see Util#isNullOrBlank(String)
	 */
	@Nullable
	private Integer convertInteger (@Nullable String immediate) {
		Integer imm=null;
		if ( !Util.isNullOrBlank( immediate ) )
			try {    // try hex
				if ( isHex( immediate ) )
					imm=Convert.hex2uInt( immediate );
				else if ( isDec( immediate ) )
					imm=Integer.parseInt( immediate );
			} catch ( Exception e ) {
				// is not an normal integer -- Imm stays null
			}
		
		if ( imm==null )
			errorLog.appendEx( lineNo, "\tImmediate Value: \"" + immediate + "\" Not Valid Integer" );
		return imm;
	}
	/**
	 Checks if immediate is valid 16 bit value, skips check if null input
	 Min[ -2^15 : -32768 ], Max[ 2^15-1 : 32767 ]
	 
	 @see Util#notNullAndInRange(Integer, int, int)
	 */
	@Nullable
	private Integer is16Bit (@Nullable Integer immediate) {
		if ( Util.notNullAndInRange( immediate, -32768, 32767 ) )
			return immediate;
		else if (immediate!=null)
			errorLog.appendEx( lineNo, "\tImmediate Value: \"" + immediate + "\" Not In (Signed 16Bit) Range" );
		return null;
	}
	/**
	 Checks if immediate is valid unsigned 26 bit value, skips check if null input
	 Min[ 0 : 0 ], Max[ 2^26-1 : 67108863 ]
	 
	 @see Util#notNullAndInRange(Integer, int, int)
	 */
	@Nullable
	private Integer isU26Bit (@Nullable Integer immediate) {
		if ( Util.notNullAndInRange( immediate, 0, 67108863 ) )
			return immediate;
		else if (immediate!=null)
			errorLog.appendEx( lineNo, "\tImmediate Value: \"" + immediate + "\" Not In (Unsigned 26Bit) Range" );
		return null;
	}
	@VisibleForTesting
	void setOpcode (String opcode) {
		this.opcode=opcode;
	}
	
}
