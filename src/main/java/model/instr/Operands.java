package model.instr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import util.validation.AddressValidation;
import util.Convert;
import util.logs.ErrorLog;

import java.util.HashMap;

/**
 Models all the possible operands an instruction can hold.
 Assumes valid inputs.
 <p>
 Methods, may return null - if it is not used by the operand.
 */
public class Operands {
	@VisibleForTesting
	protected InstrType instrType;
	private boolean immediateSet;
	//RS is only used for reading.
	//RD is only used for writing. And only used in R_types.
	//RT is used for reading by R_type,  but can be used for writing by I_types.
	
	// (add, sub): R
	// (addi): I_write
	// (lw): I_write
	// (sw): I_read
	// (j, jal) J
	// (halt, exit) EXIT
	// set unused values to - 0, 0, 0, 0, null
	//TODO refactor to factory/builder
	// and move Operand validation here:
	
	//TODO Change to using Instruction Type instead of opcode directly
	
	private Integer rs, rt, rd, immediate;
	private String label;
	
	/**
	 Expects Immediate to be valid if label is null,
	 <p> [int opcode] needs to be 1 (addi), 2 (lw) or 3 (sw).
	 
	 @throws IllegalArgumentException if label is blank
	 */
	public Operands(String opcode, @Nullable Integer rt, @NotNull String label) throws IllegalArgumentException{
		this( opcode, null, rt, null ); // rs is set to 0 at assembly
		this.label=label;
		if ( label.isBlank( ) )
			throw new IllegalArgumentException( "Operands cannot be set with blank Label! : " + opcode );
	}
	
	/**
	 <p>[int opType] needs to be 1 (addi), 2 (lw) or 3 (sw).
	 
	 @throws IllegalArgumentException opcode out of range
	 */
	public Operands(String opcode, @Nullable Integer rs, @Nullable Integer rt, @Nullable Integer immediate) throws IllegalArgumentException{ // Immediate
		this( rs, rt, null );
		this.immediate=immediate;
		
		switch ( opcode ) {
			case "addi":
			case "lw":
				this.instrType=InstrType.I_rt_write;
				break;
			case "sw":
				this.instrType=InstrType.I_rt_read;
				break;
			default:
				throw new IllegalArgumentException( " Invalid opcode for Immediate type : " + opcode );
		}
		immediateSet=(immediate!=null);
	}
	
	/**
	 InstrType.R; (add, sub) . or EXIT
	 */
	public Operands(@Nullable Integer rs, @Nullable Integer rt, @Nullable Integer rd) {
		this( );
		this.rs=rs;
		this.rt=rt;
		this.rd=rd;
	}
	
	/**
	 Intended for "EXIT" all values set to null.
	 */
	private Operands() {
		this.rs=null;
		this.rt=null;
		this.rd=null;
		this.immediate=null;
		this.label=null;
		this.instrType=InstrType.R;
		this.immediateSet=false;
	}
	
	/** Jump */
	public Operands(@NotNull String label) throws IllegalArgumentException {
		this( );
		this.label=label;
		if ( label.isBlank( ) )
			throw new IllegalArgumentException( "Operands cannot be set with blank Label! : \"" + label +"\"");
		this.instrType=InstrType.J;
	}
	
	/** Jump */
	public Operands(@NotNull Integer address) {
		this( );
		this.immediate=address;
		this.instrType=InstrType.J;
	}
	
	/**
	 @return pre-defined NULL operands for EXIT type instruction.
	 */
	@NotNull
	public static Operands getExit() {
		return new Operands( );
	}
	
	/**
	 <b>Only to be used in Assembly Phase.</b>
	 
	 <p>Adds to {@link ErrorLog}, if label match not found in labelMap
	 
	 @return success of setting the immediate - if label matches an address
	 
	 @throws IllegalArgumentException if used with null label in Operands.
	 */
	public boolean setImmediate(@NotNull ErrorLog errorLog, @NotNull HashMap<String, Integer> labelMap) throws IllegalArgumentException, IllegalStateException {
		if ( this.instrType==InstrType.R )
			throw new IllegalStateException( "Cannot setImmediate for Register type!" );
		
		if ( this.label==null || this.label.isBlank( ) )
			throw new IllegalArgumentException( "Cannot setImmediate with Blank/Null internal Label!" );
		
		if ( this.immediateSet )
			throw new IllegalStateException( "Immediate has already been set!" );
		
		if ( rs!=null )
			throw new IllegalStateException( "Cannot setImmediate when RS is Not null!" );
		
		if ( !labelMap.containsKey( this.label ) ) {
			errorLog.appendEx( "Label \"" + this.label + "\" Not Found" );
			return false;
		}
		int address=labelMap.get( this.label );
		final String invalidInstrAddr="Label: \"" + this.label + "\" points to Invalid Instruction Address";
		final String invalidDataAddr="Label: \"" + this.label + "\" points to Invalid Data Address";
		
		switch ( this.instrType ) {
			case J:
				if ( AddressValidation.isSupportedInstrAddr( address, errorLog ) )
					this.immediate=Convert.address2Imm( address );
				else
					errorLog.appendEx( invalidInstrAddr );
				break;
			case I_rt_read:
			case I_rt_write:
				if ( AddressValidation.isSupportedDataAddr( address, errorLog ) ) {
					//TODO, really it should be creating a pseudo instruction LUI before this.
					//	Split ADDR.toHexString in half, top half is loaded by LUI, bottom half is set to the IMM
					this.immediate=( address );
					this.rs=0;
				} else
					errorLog.appendEx( invalidDataAddr );
				break;
			default:
				throw new IllegalStateException( "Cannot setImmediate for " + this.instrType + " type!" );
		}
		// to avoid repeat attempts. If this method gives an error the input file needs to be edited
		this.immediateSet=true;
		return (this.immediate!=null); // returns True if Immediate has been set.
	}
	
	@Nullable
	public Integer getRs() {
		return this.rs;
	}
	
	@Nullable
	public Integer getRt() {
		return this.rt;
	}
	
	@Nullable
	public Integer getRd() {
		return this.rd;
	}
	
	/**
	 For I/J types this should be set during assembly using {@link #setImmediate}
	 
	 @return Immediate, if set, or NULL.
	 */
	@Nullable
	public Integer getImmediate() {
		return this.immediate;
	}
	
	@Nullable
	public String getLabel() {
		return this.label;
	}
	
	@NotNull
	public InstrType getInstrType() {
		return this.instrType;
	}
	
	
	@NotNull
	public enum InstrType {
		R,
		I_rt_read,	// TODO merge, there is no difference in rt_read and rt_write are treated
		I_rt_write,	// - ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		J
	}
	
	@Override public String toString ( ) {
		return "Operands{ " +
			   "instrType= " + instrType +
			   ", immediateSet= " + immediateSet +
			   ", rs= " + rs +
			   ", rt= " + rt +
			   ", rd= " + rd +
			   ", immediate= " + immediate +
			   ", label= '" + label + '\'' +
			   " }";
	}
	
}
