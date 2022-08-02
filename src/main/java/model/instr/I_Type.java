package model.instr;

import org.jetbrains.annotations.NotNull;

import util.Util;
import util.validation.InstructionValidation;

import java.util.List;

public class I_Type extends Instruction {
	
	/**{@link InstructionValidation#I_TYPE}*/
	protected I_Type (@NotNull List<String> codes, @NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{
		super( Type.IMMEDIATE, codes, opcode, RS, RT, null, IMM, null );
		
		if (!Util.notNullAndInRange( IMM, -32768, 32767))//Signed 16Bit
			throw new IllegalArgumentException("Immediate["+IMM+"] Not In Range!"); // TODO:TEST
		super.regNotInRange_Register( RS );
		super.regNotInRange_Register( RT );
	}
	
	/**{@link InstructionValidation#I_TYPE}, Label needs to be assembled into IMM value*/
	protected I_Type (@NotNull List<String> codes, @NotNull String opcode, int RS, int RT, @NotNull String label) throws IllegalArgumentException{
		super( Type.IMMEDIATE, codes, opcode, RS, RT, null, null, label );
		super.regNotInRange_Register( RS ); // redundant
		super.regNotInRange_Register( RT );
	}
	
	/**{@link InstructionValidation#I_RT_RS_IMM}*/
	public I_Type (@NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{	// Refactor to factory ?
		this( InstructionValidation.I_RT_RS_IMM, opcode, RS, RT, IMM );
	}
	
}
