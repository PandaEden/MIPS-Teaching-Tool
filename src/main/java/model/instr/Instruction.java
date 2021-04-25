package model.instr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import util.Convert;
import util.Util;
import util.logs.ErrorLog;
import util.validation.AddressValidation;
import util.validation.InstructionValidation;

import java.util.HashMap;
import java.util.List;

/**
 Expects all used variables to not be null.
 <p>For Immediate Type, where the address is set by assembly.
 <p> - it is presumed the immediate represents the full 32bit address.
 <p>For Jump Type,
 <p> - it presumes the immediate stores the address shifted 2 bits right.
 */
public abstract class Instruction {
	private final Type type;	// refactor out type in-favour of instanceOf
	protected final String opcode;
	protected Integer NPC;
	protected Integer RD;
	protected Integer RS;
	protected Integer RT;
	protected Integer IMM;
	protected final String label;
	// TODO Add lineNo
	
	/**No Validation is performed, assumed all input to be valid. {@link #assemble(ErrorLog, HashMap, int)} needs to be ran before execution.
	 <p>Errors with format may be caught during assembly.</p>
	 <p>Trying to Execution an instruction where assembly has failed will throw an exception.</p>*/
	protected Instruction (@NotNull Type type, @NotNull List<String> codes, @NotNull String opcode,
						   Integer RS, Integer RT, Integer RD, @Nullable Integer IMM, @Nullable String label)
	throws IllegalArgumentException{
		this.type=type;	// TODO is the Type Enum Necessary ?
		this.opcode=opcode;
		this.RD=RD;
		this.RS=RS;
		this.RT=RT;
		this.IMM=IMM;
		this.label=label;
		
		if ( !codes.contains( opcode ) ) // TODO - move to InstructionValidation
			throw new IllegalArgumentException("Instruction ["+opcode+"], Has not been defined in InstructionValidation");
	}
	protected void regNotInRange_Register (int reg){
		if (!Util.notNullAndInRange( reg, 0, 31 ))
			throw new IllegalArgumentException("Registers not in range!, "+toString());
	}
	
	/**
	 Returns the success of assembling the instruction & it's operands.
	 also returns True if the instruction has already been assembled.
	 
	 <p>Illegal Argument Exception may be throw is the label map
	 */
	public boolean assemble (@NotNull ErrorLog log, @NotNull HashMap<String, Integer> labelMap, int PC)
			throws IllegalArgumentException{
		if ( this.IMM==null && (!Util.isNullOrBlank(this.label)) )
			return this.setImm( log, labelMap, PC );
		return true;
	}
	
	/**
	 <b>Only to be used in Assembly Phase.</b>
	 
	 <p>Adds to {@link ErrorLog}, if label match not found in labelMap
	 
	 @return success of setting the immediate - if label matches an address
	 
	 @throws IllegalArgumentException if used with null label Operand.
	 @throws IllegalStateException error with initialisation of instruction.
	 */
	public boolean setImm(@NotNull ErrorLog errorLog, @NotNull HashMap<String, Integer> labelMap, int PC)
			throws IllegalArgumentException, IllegalStateException {
		if ( IMM==null) {
			if ( this.label==null || this.label.isBlank( ) )
				throw new IllegalArgumentException( "Cannot setImmediate with Blank/Null internal Label!" );
			
			String pfx="Label: \"" + this.label + "\"";
			if ( !labelMap.containsKey( this.label ) )
				errorLog.appendEx( pfx + " Not Found" );
			else {
				int address=labelMap.get( this.label );
				final String invalidInstrAddr=" points to Invalid Instruction Address"; // JUMP / BRANCH
				switch ( this.type ) {
					case JUMP:	// TODO, move to subclass
						if ( AddressValidation.isSupportedInstrAddr( address, errorLog ) )
							this.IMM=Convert.address2Imm( address );
						else
							errorLog.appendEx( pfx+invalidInstrAddr );
						break;
					case IMMEDIATE:	// TODO, move to subclass
						if ( InstructionValidation.I_TYPE_RT_IMM_RS.contains( opcode ) ) {
							if ( RS!=0 )
								throw new IllegalStateException( "Invalid Operands for Assembly, IMM[" + IMM + "], RS[" + RS + "]" );
							else if ( AddressValidation.isSupportedDataAddr( address, errorLog ) )
								//TODO, really it should be creating a pseudo instruction LUI before this.
								//	Split ADDR.toHexString in half, top half is loaded by LUI, bottom half is set to the IMM
								this.IMM=(address);
							else
								errorLog.appendEx( pfx+" points to Invalid Data Address" );
						} else if ( InstructionValidation.I_TYPE_RT_RS_INSTR.contains( opcode ) ) {
							if ( !AddressValidation.isSupportedInstrAddr( address, errorLog ) )
								errorLog.appendEx( pfx+invalidInstrAddr );
							else {
								this.IMM=Convert.address2Imm( address-PC );
								if ( !Util.notNullAndInRange( IMM, -32768, 32768) ) {// Signed 16bit
									errorLog.appendEx( "Offset Imm[" + IMM + "], Is not a Valid Signed 16Bit Number" );
									this.IMM=null; // so it returns false;
								}
								
							}
						}
						break;
				}
			}
		}
		return (this.IMM!=null); // returns True if Immediate has been set.
	}
	
	@Nullable
	public Integer getImmediate() {
		return IMM;
	}
	
	@Nullable
	public String getOpcode() {
		return opcode;
	}
	@Nullable
	public Integer getRD() {
		return RD;
	}
	@Nullable
	public Integer getRS() {
		return RS;
	}
	@Nullable
	public Integer getRT() {
		return RT;
	}
	
	public Type getType() {
		return type;
	}
	
	@Override public String toString() {
		return "Instruction{" +
			   " opcode= '" + opcode + '\'' +
			   ", type= " + type +
			   ", RD= " + RD +
			   ", RS= " + RS +
			   ", RT= " + RT +
			   ", IMM= " + IMM +
			   (label==null?"":", label= '" + label + '\'') +
			   " }";
	}
	
	@Override public boolean equals (Object o) {
		return this.toString().equals( o.toString() );
	}
	public enum Type {REGISTER, IMMEDIATE, JUMP, NOP}
}
