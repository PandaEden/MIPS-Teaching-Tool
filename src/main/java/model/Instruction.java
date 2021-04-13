package model;

import org.jetbrains.annotations.NotNull;

import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Operands;

import setup.Parser;

import util.Convert;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;

import java.util.HashMap;

/**
 Expects all used variables to not be null.
 <p>For Immediate Type, where the address is set by assembly.
 <p> - it is presumed the immediate represents the full 32bit address.
 <p>For Jump Type,
 <p> - it presumes the immediate stores the address shifted 2 bits right.
 */
public class Instruction {
	private final Type type;
	private final Operands operands;
	protected String ins;
	protected Integer NPC;
	protected Integer RD;
	protected Integer RS;
	protected Integer RT;
	protected Integer IMM;
	protected String label;
	private boolean assembled=false;
	
	/**
	 @param ins - NotNull use code 'no_ins' if not an instruction
	 */
	Instruction(String ins, Operands operands) {
		this.ins=ins;
		this.type=ins2Type( ins );
		this.operands=operands;
	}
	
	//TODO refactor to use the same list that validate uses
	private static Type ins2Type(String ins) {
		switch ( ins ) {
			case "add":
			case "sub":
				return Type.REGISTER;
			case "addi":
			case "lw":
			case "sw":
				return Type.IMMEDIATE;
			case "j":
			case "jal":
				return Type.JUMP;
			default:
				return Type.EXIT;
		}
	}
	
	/**
	 <b>This builder only pairs the opcode, with the operand, It performs no validation</b>
	 <p>Errors with format may be caught during assembly.</p>
	 <p>Trying to Execute an instruction where assembly has failed will throw an exception.</p>
	 */
	public static Instruction buildInstruction(@NotNull String opcode, @NotNull Operands operands) {
		Type type=ins2Type( opcode );
		
		//TODO refactor into all being in One Class,
		if ( type==Type.REGISTER )
			return new R_Type( opcode, operands );
		else if ( type==Type.IMMEDIATE )
			return new I_Type( opcode, operands );
		else if ( type==Type.JUMP )
			return new J_Type( opcode, operands );
		else
			return new Instruction( opcode, operands );
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
		if ( !assembled )
			throw new IllegalStateException( ins + " must be Assembled before Execution " + Convert.int2Hex( PC ) );
		
		String dash=" ---- ";
		executionLog.append( "\n\t" + dash + Convert.int2Hex( PC ) + dash + type.name( ) + " Type Instruction >> \"" + ins + "\":" );
		NPC=PC + 4;
		action( dataMem, regBank, executionLog );
		return NPC;
	}
	
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog) {
		//EXIT do nothing, - set NPC to null
		regBank.noAction( );
		dataMem.noAction( );
		NPC=null;
	}
	
	/**
	 Returns the success of assembling the instruction & it's operands.
	 also returns True if the instruction has already been assembled.
	 
	 <p>Illegal Argument Exception may be throw is the label map
	 
	 @see Operands#setImmediate(ErrorLog, HashMap)
	 */
	public boolean assemble(@NotNull ErrorLog log, @NotNull HashMap<String, Integer> labelMap) throws IllegalStateException, IllegalArgumentException{
		String opLabel=operands.getLabel( );
		if ( this.IMM==null && (!Parser.isNullOrBlank(opLabel)) )
			operands.setImmediate( log, labelMap );
		
		readOperands( );
		if ( this instanceof R_Type ) {
			assembled=(this.RS!=null && this.RT!=null && this.RD!=null)
					  && this.IMM==null;
		} else if ( this instanceof I_Type ) {
			assembled=(this.RS!=null && this.RT!=null && this.IMM!=null)
					  && this.RD==null;
		} else if ( this instanceof J_Type ) {
			assembled=this.IMM!=null
					  && (this.RS==null && this.RT==null && this.RD==null);
		} else {
			assembled=true; // Exit Type
		}
		
		return assembled;
	}
	/** Uses {@link Convert#imm2Address(Integer)} on {@link #IMM} */
	protected Integer shiftImm(ExecutionLog executionLog){
		int ADDR = Convert.imm2Address(IMM);
		executionLog.append( "\tLeft Shifting IMMEDIATE By 2 = " +  Convert.int2Hex( IMM )
							 + " << " + 2 + " ==> ["+ADDR+" === " + Convert.int2Hex( ADDR ) +"]");
		return ADDR;
	}
	
	// Reads from the Operands object, and sets the appropriate values
	protected void readOperands() {
		this.RD=operands.getRd( );
		this.RS=operands.getRs( );
		this.RT=operands.getRt( );
		this.IMM=operands.getImmediate( );
		this.label=operands.getLabel( );
	}
	
	private enum Type {REGISTER, IMMEDIATE, JUMP, EXIT}
	
}
