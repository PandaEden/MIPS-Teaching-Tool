package model.instr;

import Util.Logs.ErrorLog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 Models all the possible operands an instruction can hold.
 
 Methods, may return null - if it is not used by the operand.
 */
public class Operands{
	private static final Operands exitOperands = new Operands();
	private InstrType instrType;
	
	//RS is only used for reading.
	//RD is only used for writing. And only used in R_types.
	//RT is used for reading by R_type,  but can be used for writing by I_types.
	
	// 0 - (add, sub): R
	// 1 - (addi): I_write
	// 2 - (lw): I_write
	// 3 - (sw): I_read
	// 4 - (j, jal) J
	// 5 - EXIT (halt, exit)
	// set unused values to - 0, 0, 0, 0, null
	
	private Integer rs, rt, rd, immediate;
	private String label;
	
	/**
	 Intended for "EXIT" all values set to null.
	 */
	private Operands(){
		this.rs =null; this.rt =null; this.rd =null; this.immediate =null; this.label=null;
		this.instrType = InstrType.R;
	}
	
	/**
	 @return pre-defined NULL operands for EXIT type instruction.
	 */
	public static Operands getExit(){
		return exitOperands;
	}
	
	/**
	 InstrType.R; (add, sub) . or EXIT
	 */
	Operands(@Nullable Integer rs, @Nullable Integer rt, @Nullable Integer rd){
		this(); this.rs = rs; this.rt = rt; this.rd = rd;
	}
	
	/**
	 	Expects Immediate to be valid if label is null,
	 <p> [int opcode] needs to be 1 (addi), 2 (lw) or 3 (sw).
	 @throws IllegalArgumentException if Immediate AND label are null, opcode out of range
	 */
	Operands(int opcode, @Nullable Integer rs, @Nullable Integer rt, @Nullable Integer immediate, @NotNull String label){
		this(opcode,rs, rt,immediate); this.label=label;
	}
	
	/**
	 <p>[int opType] needs to be 1 (addi), 2 (lw) or 3 (sw).
	 @throws IllegalArgumentException	opcode out of range
	 */
	Operands(int opcode, @Nullable Integer rs, @Nullable Integer rt, @Nullable Integer immediate){ // Immediate
		this(rs, rt,0); this.immediate =immediate; this.instrType = InstrType.I_write;
		
		switch (opcode){
			case 1:
			case 2:
				break; // I_write already set
			case 3:
				this.instrType = InstrType.I_read;
				break;
			default:
				throw new IllegalArgumentException(" defining operands not valid opcode for Immediate : "+opcode);
		}
	}
	/**Jump*/
	Operands(@NotNull String label){
		this(); this.label=label; this.instrType = InstrType.J;
	}
	/**Jump*/
	Operands(@NotNull Integer immediate){
		this(); this.immediate =immediate; this.instrType = InstrType.J;
	}
	
	/**
	 For I/J types this should be set during assembly using {@link #setImmediate}
	 @return Immediate, if set, or NULL.
	 */
	public Integer getImmediate(){
		return immediate==null?0:immediate;
	}
	
	/**
	 @return label, may be NULL.
	 */
	public  String getLabel(){
		return label;
	}
	
	public InstrType getInstrType(){
		return instrType;
	}
	
	/**
	 <b>Only to be used in Assembly Phase.</b>
	 
	 <p>Adds to {@link ErrorLog}, if label match not found in labelMap
	 
	 @return success of setting the immediate - if label matches an address
	 @throws NullPointerException if used with null label in Operands.
	 */
	public boolean setImmediate(@NotNull ErrorLog errorLog,@NotNull HashMap<String,Integer> labelMap){
		if (label.isBlank()){
			throw new NullPointerException("Label is Null, trying to setImmediate");
		}
		if (!labelMap.containsKey(label)){
			errorLog.append("Label \""+label+"\" Not Found!");
			return false;
		}
		immediate=labelMap.get(label);
		return true;
	}
	
	public Integer getRs(){
		return rs==null?0:rs;
	}
	
	public Integer getRt(){
		return rt==null?0:rt;
	}
	
	public Integer getRd(){
		return rd==null?0:rd;
	}
	
	public enum InstrType{
		R,
		I_read,
		I_write,
		J
	}
}
