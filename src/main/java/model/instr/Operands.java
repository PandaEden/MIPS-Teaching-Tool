package model.instr;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Convert;
import util.Validate;
import util.logs.ErrorLog;

import java.util.HashMap;

/**
 Models all the possible operands an instruction can hold.
 Assumes valid inputs.
 <p>
 Methods, may return null - if it is not used by the operand.
 */
public class Operands{
	private static final Operands exitOperands = new Operands();
	private InstrType instrType;
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
	
	//TODO change this to return NULL for values not used. e.g. R doesn't use labels, or Immediate
	
	private Integer rs, rt, rd, immediate;
	private String label;
	
	/**
	 Intended for "EXIT" all values set to null.
	 */
	private Operands(){
		this.rs = null;
		this.rt = null;
		this.rd = null;
		this.immediate = null;
		this.label = null;
		this.instrType = InstrType.R;
		this.immediateSet = false;
	}
	
	/**
	 InstrType.R; (add, sub) . or EXIT
	 */
	Operands(@Nullable Integer rs, @Nullable Integer rt, @Nullable Integer rd){
		this();
		this.rs = rs;
		this.rt = rt;
		this.rd = rd;
	}
	
	/**
	 Expects Immediate to be valid if label is null,
	 <p> [int opcode] needs to be 1 (addi), 2 (lw) or 3 (sw).
	 
	 @throws IllegalArgumentException if Immediate AND label are null, opcode out of range
	 */
	Operands(String opcode, @Nullable Integer rt, @NotNull String label){
		this(opcode, null, rt, null);
		this.label = label;
		if (label.isBlank())
			throw new IllegalArgumentException("Operands cannot be set with blank Label!");
	}
	
	/**
	 <p>[int opType] needs to be 1 (addi), 2 (lw) or 3 (sw).
	 
	 @throws IllegalArgumentException opcode out of range
	 */
	Operands(String opcode, @Nullable Integer rs, @Nullable Integer rt, @Nullable Integer immediate){ // Immediate
		this(rs, rt, 0);
		this.immediate = immediate;
		this.instrType = InstrType.I_write;
		
		switch (opcode) {
			case "addi":
			case "lw":
				break; // I_write already set
			case "sw":
				this.instrType = InstrType.I_read;
				break;
			default:
				throw new IllegalArgumentException(" Invalid opcode for Immediate type : "+opcode);
		}
		immediateSet = (immediate==null);
	}
	
	/** Jump */
	Operands(@NotNull String label){
		this();
		this.label = label;
		this.instrType = InstrType.J;
	}
	
	/** Jump */
	Operands(@NotNull Integer address){
		this();
		this.immediate = address;
		this.instrType = InstrType.J;
	}
	
	/**
	 @return pre-defined NULL operands for EXIT type instruction.
	 */
	@NotNull
	public static Operands getExit(){
		return exitOperands;
	}
	
	/**
	 For I/J types this should be set during assembly using {@link #setImmediate}
	 
	 @return Immediate, if set, or NULL.
	 */
	@NotNull
	public Integer getImmediate(){
		return this.immediate==null ? 0 : this.immediate;
	}
	
	/**
	 @return label, may be NULL.
	 */
	@Nullable
	public String getLabel(){
		return this.label;
	}
	
	@NotNull
	public InstrType getInstrType(){
		return this.instrType;
	}
	
	/**
	 <b>Only to be used in Assembly Phase.</b>
	 
	 <p>Adds to {@link ErrorLog}, if label match not found in labelMap
	 
	 @return success of setting the immediate - if label matches an address
	 
	 @throws IllegalArgumentException if used with null label in Operands.
	 */
	public boolean setImmediate(@NotNull ErrorLog errorLog, @NotNull HashMap<String, Integer> labelMap){
		Validate validate = new Validate(errorLog);
		final String invalidInstrAddr = "Label points to Invalid Instruction Address";
		final String invalidDataAddr = "Label points to Invalid Data Address";
		
		if (this.instrType==InstrType.R)
			throw new IllegalArgumentException("Cannot setImmediate for Register type!");
		
		if (this.label==null || this.label.isBlank())
			throw new IllegalArgumentException("Cannot setImmediate with Blank/Null internal Label!");
		
		if (this.immediate!=null || rs!=null)
			throw new IllegalArgumentException("Cannot setImmediate when Immediate/RS are not null!");
		
		if (this.immediateSet)
			throw new IllegalArgumentException("Immediate has already been set");
		
		if (!labelMap.containsKey(label)) {
			errorLog.append("Label \""+label+"\" Not Found!");
			return false;
		}
		int address = labelMap.get(this.label);
		
		switch (this.instrType) {
			case J:
				if (validate.isSupportedInstrAddr(address))
					this.immediate = Convert.address2Imm(address);
				else
					errorLog.append(invalidInstrAddr);
				break;
			case I_read:
			case I_write:
				if (validate.isSupportedDataAddr(address))
					this.immediate = Convert.address2Imm(address);
				else
					errorLog.append(invalidDataAddr);
				break;
			default:
				throw new IllegalArgumentException("Cannot setImmediate for "+this.instrType+" type!");
		}
		// to avoid repeat attempts. If this method gives an error the input file needs to be edited
		immediateSet = true;
		return (immediate!=null); // returns True if Immediate has been set.
	}
	
	@Nullable
	public Integer getRs(){
		return this.rs==null ? 0 : this.rs;
	}
	
	@Nullable
	public Integer getRt(){
		return this.rt==null ? 0 : this.rt;
	}
	
	@Nullable
	public Integer getRd(){
		return this.rd==null ? 0 : this.rd;
	}
	
	@NotNull
	public enum InstrType{
		R,
		I_read,
		I_write,
		J
	}
}
