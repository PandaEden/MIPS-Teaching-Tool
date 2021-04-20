package model.instr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import model.components.DataMemory;
import model.components.RegisterBank;

import util.Convert;
import util.Util;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
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
	private final Type type;
	protected final String opcode;
	protected Integer NPC;
	protected final int RD;
	protected final int RS;
	protected final int RT;
	protected Integer IMM;
	protected final String label;
	// TODO Add lineNo
	
	/**No Validation is performed, assumed all input to be valid. {@link #assemble(ErrorLog, HashMap)} needs to be ran before execution.
	 <p>Errors with format may be caught during assembly.</p>
	 <p>Trying to Execute an instruction where assembly has failed will throw an exception.</p>*/
	protected Instruction (@NotNull Type type, @NotNull List<String> codes, @NotNull String opcode,
						   int RS, int RT, int RD, @Nullable Integer IMM, @Nullable String label)
	throws IllegalArgumentException{
		this.type=type;	// TODO is the Type Enum Necessary ?
		this.opcode=opcode;
		this.RD=RD;
		this.RS=RS;
		this.RT=RT;
		this.IMM=IMM;
		this.label=label;
		
		if ( !codes.contains( opcode ) ) // TODO - rename to InstructionValidation
			throw new IllegalArgumentException("Instruction ["+opcode+"], Has not been defined in InstructionValidation");
		regNotInRange( RD );
		regNotInRange( RS );
		regNotInRange( RT );
	}
	private void regNotInRange (int reg){
		if (!Util.notNullAndInRange( reg, 0, 31 ))
			throw new IllegalArgumentException("Registers not in range!, "+toString());
	}
	
	/**
	 Executes the {@link Instruction}, and returns an Incremented Program Counter
	 or , NULL if the instruction is "EXIT".
	 <p>
	 Executing instructions using BASE+Offset addresses may throw an InvalidArgumentException.
	 
	 @throws IllegalStateException if not Assembled/ Assembly failed!
	 */
	public Integer execute(int PC, @NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						   @NotNull ExecutionLog executionLog) throws IndexOutOfBoundsException, IllegalArgumentException {
		if ( IMM==null )
			throw new IllegalStateException( opcode + " must be Assembled before Execution " + Convert.int2Hex( PC ) );
		
		String dash=" ---- ";
		executionLog.append( "\n\t" + dash + Convert.int2Hex( PC ) + dash + type.name( ) + " Type Instruction >> \"" + opcode + "\":" );
		NPC=PC + 4;
		action( dataMem, regBank, executionLog );
		return NPC;
	}
	
	protected abstract void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank, @NotNull ExecutionLog executionLog);
	
	/** Uses {@link Convert#imm2Address(Integer)} on {@link #IMM} */
	protected Integer shiftImm(ExecutionLog executionLog){
		int ADDR = Convert.imm2Address(IMM);
		executionLog.append( "\tLeft Shifting IMMEDIATE By 2 = " +  Convert.int2Hex( IMM )
							 + " << " + 2 + " ==> ["+ADDR+" === " + Convert.int2Hex( ADDR ) +"]");
		return ADDR;
	}
	
	/**
	 Returns the success of assembling the instruction & it's operands.
	 also returns True if the instruction has already been assembled.
	 
	 <p>Illegal Argument Exception may be throw is the label map
	 */
	public boolean assemble(@NotNull ErrorLog log, @NotNull HashMap<String, Integer> labelMap)
			throws IllegalArgumentException{
		if ( this.IMM==null && (!Util.isNullOrBlank(this.label)) )
			return this.setImm( log, labelMap );
		return true;
	}
	
	/**
	 <b>Only to be used in Assembly Phase.</b>
	 
	 <p>Adds to {@link ErrorLog}, if label match not found in labelMap
	 
	 @return success of setting the immediate - if label matches an address
	 
	 @throws IllegalArgumentException if used with null label Operand.
	 @throws IllegalStateException error with initialisation of instruction.
	 */
	public boolean setImm(@NotNull ErrorLog errorLog, @NotNull HashMap<String, Integer> labelMap)
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
						if ( InstructionValidation.I_TYPE_MEM_ACCESS.contains( opcode ) ) {
							if ( RS!=0 )
								throw new IllegalStateException( "Invalid Operands for Assembly, IMM[" + IMM + "], RS[" + RS + "]" );
							else if ( AddressValidation.isSupportedDataAddr( address, errorLog ) )
								//TODO, really it should be creating a pseudo instruction LUI before this.
								//	Split ADDR.toHexString in half, top half is loaded by LUI, bottom half is set to the IMM
								this.IMM=(address);
							else
								errorLog.appendEx( pfx+" points to Invalid Data Address" );
						}
						break;
				}
			}
		}
		return (this.IMM!=null); // returns True if Immediate has been set.
	}
	
	public Integer getImmediate( ) {
		return IMM;
	}
	
	public String getOpcode ( ) {
		return opcode;
	}
	
	@Override public String toString ( ) {
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
